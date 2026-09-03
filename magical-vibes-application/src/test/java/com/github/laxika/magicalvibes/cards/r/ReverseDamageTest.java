package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReverseDamage.class, GrizzlyBears.class, LightningBolt.class, ProdigalSorcerer.class})
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
    @DisplayName("Prevents damage from a chosen spell and gains that much life")
    void preventsDamageFromChosenSpellAndGainsLife() {
        harness.setLife(player1, 20);
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        castReverseDamage(player1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bolt.getId());
        harness.handlePermanentChosen(player1, bolt.getId());
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

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private void castReverseDamage(Player player) {
        harness.castFromHand(player, new ReverseDamage(), "{1}{W}{W}");
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
