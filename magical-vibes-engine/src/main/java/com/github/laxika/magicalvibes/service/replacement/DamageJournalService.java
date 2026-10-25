package com.github.laxika.magicalvibes.service.replacement;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ResolutionDamageEvent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.replacement.DamageEvent;
import com.github.laxika.magicalvibes.model.replacement.DamageJournal;
import com.github.laxika.magicalvibes.model.replacement.MillEvent;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Keeps the {@link DamageJournal} of the effect being resolved. {@code EffectResolutionService}
 * opens a pass before each effect and closes it afterwards; damage started directly by the effect
 * asks {@link #next} how to proceed, and a mill it starts asks {@link #nextMill}. Damage that other damage causes (a redirect, a reflection) is
 * not journaled: it belongs to the event that caused it.
 */
@Service
public class DamageJournalService {

    /** Journal index base for combined simultaneous damage, clear of every effect index. */
    private static final int SIMULTANEOUS_DAMAGE_JOURNAL = -1_000_000;

    /** How a damage event the resolving effect starts should proceed. */
    public sealed interface Step {

        /** No journal applies; the event runs and is dealt as soon as its effects are applied. */
        record Untracked() implements Step {}

        /** The event runs, filling journal slot {@code slot}. */
        record Track(int slot) implements Step {}

        /** The event was dealt on an earlier pass; it reports {@code dealt} again. */
        record Replay(int dealt) implements Step {}

        /** The event's effects were ordered after the last pass; deal it now. */
        record FinishReady(int slot, DamageEvent event) implements Step {}

        /** An earlier event of this pass awaits its player; this one waits for the next pass. */
        record Hold() implements Step {}
    }

    /** How a mill the resolving effect starts should proceed. */
    public sealed interface MillStep {

        /** No journal applies; the mill happens as soon as its replacement effects are applied. */
        record Untracked() implements MillStep {}

        /** The mill runs, filling journal slot {@code slot}. */
        record Track(int slot) implements MillStep {}

        /** The mill happened on an earlier pass; it reports {@code milled} again without milling. */
        record Replay(DamageJournal.Slot.Milled milled) implements MillStep {}

        /** The mill's replacement effects were ordered after the last pass; mill it now. */
        record FinishReady(int slot, MillEvent event) implements MillStep {}

        /** An earlier event of this pass awaits its player; this mill waits for the next pass. */
        record Hold() implements MillStep {}
    }

    /** Opens a pass over effect {@code effectIndex} of {@code entry}. */
    public void beginEffect(GameData gameData, StackEntry entry, int effectIndex) {
        DamageJournal journal = gameData.damageJournal;
        if (journal == null || !journal.isFor(entry.getTargetableId(), effectIndex)) {
            journal = new DamageJournal(entry.getTargetableId(), effectIndex);
            gameData.damageJournal = journal;
        }
        journal.beginPass();
    }

    /**
     * Opens a pass over the combined damage of simultaneous damage event {@code event}, dealt once the event's steps have resolved. A prompt it raises is answered like
     * one an effect raises: the pass runs again and replays what was already dealt.
     */
    public void beginSimultaneousDamage(GameData gameData, ResolutionDamageEvent event) {
        int index = SIMULTANEOUS_DAMAGE_JOURNAL - event.event();
        DamageJournal journal = gameData.damageJournal;
        if (journal == null || !journal.isFor(event.stackObjectId(), index)) {
            journal = new DamageJournal(event.stackObjectId(), index);
            gameData.damageJournal = journal;
        }
        journal.beginPass();
    }

    /**
     * Closes the pass. While an event of the effect is still to be dealt, the effect is resolved
     * again once its player has answered; otherwise the journal is done.
     */
    public void endEffect(GameData gameData) {
        DamageJournal journal = gameData.damageJournal;
        if (journal == null) {
            return;
        }
        journal.endPass();
        if (journal.hasUnfinishedEvents()) {
            journal.setRerunRequested(true);
            gameData.rerunCurrentEffectAfterInteraction = true;
            return;
        }
        if (journal.isRerunRequested()) {
            gameData.rerunCurrentEffectAfterInteraction = false;
        }
        gameData.damageJournal = null;
    }

    /** Whether damage starting now is the resolving effect's own rather than damage other damage caused. */
    public boolean isEffectDamage(GameData gameData) {
        DamageJournal journal = gameData.damageJournal;
        return journal != null && journal.isActive() && !journal.isProcessingDamage();
    }

    /** How the next damage event should proceed; nested damage is never journaled. */
    public Step next(GameData gameData) {
        DamageJournal journal = gameData.damageJournal;
        if (journal == null || !journal.isActive() || journal.isProcessingDamage()) {
            return new Step.Untracked();
        }
        if (journal.isBlocked()) {
            journal.claim(new DamageJournal.Slot.Held());
            return new Step.Hold();
        }
        DamageJournal.Slot slot = journal.nextSlot();
        if (slot instanceof DamageJournal.Slot.Done done) {
            journal.claim(done);
            return new Step.Replay(done.dealt());
        }
        if (slot instanceof DamageJournal.Slot.Ready ready) {
            return new Step.FinishReady(journal.claim(ready), ready.event());
        }
        if (slot instanceof DamageJournal.Slot.Awaiting) {
            journal.claim(slot);
            journal.block();
            return new Step.Hold();
        }
        return new Step.Track(journal.claim(new DamageJournal.Slot.Awaiting()));
    }

    /** How the next mill should proceed; a mill that damage causes is never journaled. */
    public MillStep nextMill(GameData gameData) {
        DamageJournal journal = gameData.damageJournal;
        if (journal == null || !journal.isActive() || journal.isProcessingDamage()) {
            return new MillStep.Untracked();
        }
        if (journal.isBlocked()) {
            journal.claim(new DamageJournal.Slot.Held());
            return new MillStep.Hold();
        }
        DamageJournal.Slot slot = journal.nextSlot();
        if (slot instanceof DamageJournal.Slot.Milled milled) {
            journal.claim(milled);
            return new MillStep.Replay(milled);
        }
        if (slot instanceof DamageJournal.Slot.MillReady ready) {
            return new MillStep.FinishReady(journal.claim(ready), ready.event());
        }
        if (slot instanceof DamageJournal.Slot.Awaiting) {
            journal.claim(slot);
            journal.block();
            return new MillStep.Hold();
        }
        return new MillStep.Track(journal.claim(new DamageJournal.Slot.Awaiting()));
    }

    /** Whether an event of the resolving effect's current pass waits for its player. */
    public boolean isHoldingEvents(GameData gameData) {
        DamageJournal journal = gameData.damageJournal;
        return journal != null && journal.isActive() && journal.isBlocked();
    }

    /** Records that the mill of {@code slot} has happened and what it reported. */
    public void milled(GameData gameData, int slot, List<Card> cards, int nonlandCards) {
        DamageJournal journal = gameData.damageJournal;
        if (journal != null && journal.get(slot) != null) {
            journal.set(slot, new DamageJournal.Slot.Milled(cards, nonlandCards));
        }
    }

    /**
     * Records that {@code event}, whose replacement effects have all been applied, is milled when
     * its effect is resolved again. Returns {@code false} when no journal holds the slot, so the
     * caller mills it now.
     */
    public boolean millReady(GameData gameData, MillEvent event) {
        DamageJournal journal = gameData.damageJournal;
        if (event.journalSlot() < 0 || journal == null || journal.get(event.journalSlot()) == null) {
            return false;
        }
        journal.set(event.journalSlot(), new DamageJournal.Slot.MillReady(event));
        return true;
    }

    /** Marks entry into damage processing, so damage it causes is not journaled. */
    public void enter(GameData gameData) {
        if (gameData.damageJournal != null) {
            gameData.damageJournal.enterDamage();
        }
    }

    public void exit(GameData gameData) {
        if (gameData.damageJournal != null) {
            gameData.damageJournal.exitDamage();
        }
    }

    /** Records that the event of {@code slot} has been dealt. */
    public void done(GameData gameData, int slot, int dealt) {
        DamageJournal journal = gameData.damageJournal;
        if (journal != null && journal.get(slot) != null) {
            journal.set(slot, new DamageJournal.Slot.Done(dealt));
        }
    }

    /** Records that the event of {@code slot} waits for its player; later events of the pass wait too. */
    public void awaiting(GameData gameData, int slot) {
        DamageJournal journal = gameData.damageJournal;
        if (journal != null && journal.get(slot) != null) {
            journal.set(slot, new DamageJournal.Slot.Awaiting());
            journal.block();
        }
    }

    /**
     * Records that {@code event}, whose effects have all been applied, is dealt when its effect is
     * resolved again. Returns {@code false} when no journal holds the slot, so the caller deals it now.
     */
    public boolean ready(GameData gameData, DamageEvent event) {
        DamageJournal journal = gameData.damageJournal;
        if (event.journalSlot() < 0 || journal == null || journal.get(event.journalSlot()) == null) {
            return false;
        }
        journal.set(event.journalSlot(), new DamageJournal.Slot.Ready(event));
        return true;
    }
}
