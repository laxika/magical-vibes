package com.github.laxika.magicalvibes.service.replacement;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.replacement.DamageEvent;
import com.github.laxika.magicalvibes.model.replacement.DamageJournal;
import org.springframework.stereotype.Service;

/**
 * Keeps the {@link DamageJournal} of the effect being resolved. {@code EffectResolutionService}
 * opens a pass before each effect and closes it afterwards; damage started directly by the effect
 * asks {@link #next} how to proceed. Damage that other damage causes (a redirect, a reflection) is
 * not journaled: it belongs to the event that caused it.
 */
@Service
public class DamageJournalService {

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
