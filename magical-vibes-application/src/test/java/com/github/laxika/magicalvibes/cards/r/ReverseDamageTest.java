package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Blaze.class, GrizzlyBears.class, ProdigalSorcerer.class, ReverseDamage.class, Shock.class})
class ReverseDamageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Reverse Damage prompts for a source choice")
    void resolvingPromptsForSourceChoice() {
        castReverseDamage(player1);
        addReadyCreature(player2);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Choosing a source records a one-shot life-gain prevention shield")
    void choosingSourceRecordsShield() {
        castReverseDamage(player1);
        Permanent creature = addReadyCreature(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId())
                        && s.sourceId().equals(creature.getId())
                        && s.gainLife());
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen source and gains that much life")
    void preventsDamageAndGainsLife() {
        harness.setLife(player1, 20);
        castReverseDamage(player1);
        Permanent creature = addReadyCreature(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        creature.setAttacking(true);
        resolveCombat(player2);

        // 2 damage prevented, 2 life gained
        harness.assertLife(player1, 22);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Damage from the chosen source to a creature you control is not prevented")
    void chosenSourceDamageToControlledCreatureIsNotPrevented() {
        harness.setLife(player1, 20);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        castReverseDamage(player1);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sorcerer.getId());

        int sorcererIndex = gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer);
        harness.activateAbility(player2, sorcererIndex, null, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId())
                        && s.sourceId().equals(sorcerer.getId()));
    }

    @Test
    @DisplayName("Only the next damage event from the chosen source is prevented")
    void onlyNextDamageFromChosenSourceIsPrevented() {
        harness.setLife(player1, 20);
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        castReverseDamage(player1);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sorcerer.getId());

        int sorcererIndex = gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer);
        harness.activateAbility(player2, sorcererIndex, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();

        sorcerer.untap();
        harness.activateAbility(player2, sorcererIndex, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Prevents damage from a chosen spell and gains that much life")
    void preventsDamageFromChosenSpellAndGainsLife() {
        harness.setLife(player1, 20);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        castReverseDamage(player1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(shock.getId());
        harness.handlePermanentChosen(player1, shock.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents damage from a chosen spell and gains that much life")
    void preventsDamageFromChosenSpellAndGainsLifeUpstreamReview() {
        harness.setLife(player1, 20);
        Blaze blaze = new Blaze();
        harness.setHand(player2, List.of(blaze));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, 3, player1.getId());
        castReverseDamage(player1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(blaze.getId());
        harness.handlePermanentChosen(player1, blaze.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("A chosen permanent spell remains the source after it resolves")
    void preventsDamageFromPermanentSpellAfterItResolves() {
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        ProdigalSorcerer sorcererSpell = new ProdigalSorcerer();
        harness.castFromHand(player2, sorcererSpell, "{2}{U}");
        castReverseDamage(player1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(sorcererSpell.getId());
        harness.handlePermanentChosen(player1, sorcererSpell.getId());
        harness.passBothPriorities();

        Permanent sorcerer = findPermanent(player2, "Prodigal Sorcerer");
        sorcerer.setSummoningSick(false);
        int sorcererIndex = gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer);
        harness.activateAbility(player2, sorcererIndex, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("A different source still deals damage; the shield is untouched")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        castReverseDamage(player1);
        Permanent chosen = addReadyCreature(player2);
        Permanent other = addReadyCreature(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        castReverseDamage(player1);
        Permanent creature = addReadyCreature(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private void castReverseDamage(Player player) {
        harness.castFromHand(player, new ReverseDamage(), "{1}{W}{W}");
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    @Test
    @DisplayName("Damage to a creature does not consume the player-only shield")
    void damageToCreatureDoesNotConsumePlayerShield() {
        harness.setLife(player1, 20);
        Permanent source = addCreatureReady(player2, new ProdigalSorcerer());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castReverseDamage(player1);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        int sourceIndex = gd.playerBattlefields.get(player2.getId()).indexOf(source);
        harness.activateAbility(player2, sourceIndex, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);

        harness.performUntapStep(player2);
        sourceIndex = gd.playerBattlefields.get(player2.getId()).indexOf(source);
        harness.activateAbility(player2, sourceIndex, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }
}
