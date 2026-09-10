package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.Heartstone;
import com.github.laxika.magicalvibes.cards.s.SoltariChampion;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThalakosDeceiver.class, YouthfulKnight.class, Heartstone.class, SoltariChampion.class})
class ThalakosDeceiverTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new ThalakosDeceiver());
        attacker.setAttacking(true);
        return attacker;
    }

    private void advanceToUnblockedMay() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting the may sacrifices it and permanently gains control of the target creature")
    void acceptSacrificeAndGainControl() {
        Permanent attacker = addAttacker();
        Permanent target = new Permanent(new YouthfulKnight());
        gd.playerBattlefields.get(player2.getId()).add(target);

        advanceToUnblockedMay();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Thalakos Deceiver");
        harness.assertInGraveyard(player1, "Thalakos Deceiver");
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.newestControlEffectFor(target.getId()).duration())
                .isEqualTo(com.github.laxika.magicalvibes.model.effect.EffectDuration.PERMANENT);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("The target choice offers creatures but not artifacts")
    void targetChoiceOffersCreaturesOnly() {
        addAttacker();
        Permanent target = new Permanent(new YouthfulKnight());
        Permanent artifact = new Permanent(new Heartstone());
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.playerBattlefields.get(player2.getId()).add(artifact);

        advanceToUnblockedMay();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .doesNotContain(artifact.getId());
    }

    @Test
    @DisplayName("Declining the may keeps it on the battlefield and does not change control")
    void declineKeepsCreature() {
        addAttacker();
        Permanent target = new Permanent(new YouthfulKnight());
        gd.playerBattlefields.get(player2.getId()).add(target);

        advanceToUnblockedMay();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Thalakos Deceiver");
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.controlEffectsFor(target.getId())).isEmpty();
    }

    @Test
    @DisplayName("An illegal target prevents the source from being sacrificed")
    void illegalTargetPreventsResolution() {
        Permanent attacker = addAttacker();
        Permanent target = new Permanent(new YouthfulKnight());
        gd.playerBattlefields.get(player2.getId()).add(target);

        advanceToUnblockedMay();
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thalakos Deceiver");
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card.getName().equals("Youthful Knight"));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.controlEffectsFor(target.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("If the source leaves first, accepting the may cannot gain control")
    void sourceLeavingBeforeResolutionPreventsControl() {
        Permanent attacker = addAttacker();
        Permanent target = new Permanent(new YouthfulKnight());
        gd.playerBattlefields.get(player2.getId()).add(target);

        advanceToUnblockedMay();
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, attacker));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.controlEffectsFor(target.getId())).isEmpty();
    }

    @Test
    @DisplayName("A shadow creature can block it, so the ability does not trigger")
    void blockedByShadowCreatureDoesNotTrigger() {
        Permanent blocker = addCreatureReady(player2, new SoltariChampion());
        Permanent attacker = addAttacker();

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Thalakos Deceiver");
    }
}
