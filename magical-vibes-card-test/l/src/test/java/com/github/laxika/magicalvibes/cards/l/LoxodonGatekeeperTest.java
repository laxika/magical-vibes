package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.e.ElvesOfDeepShadow;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FistsOfIronwood;
import com.github.laxika.magicalvibes.cards.p.Putrefy;
import com.github.laxika.magicalvibes.cards.s.SuppressionField;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoxodonGatekeeper.class, BorosSignet.class, ElvesOfDeepShadow.class,
        Forest.class, SuppressionField.class, FistsOfIronwood.class, Putrefy.class})
class LoxodonGatekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's artifacts, creatures, and lands enter tapped")
    void opponentsArtifactsCreaturesAndLandsEnterTapped() {
        harness.addToBattlefield(player1, new LoxodonGatekeeper());
        harness.setHand(player2, List.of(new BorosSignet(), new ElvesOfDeepShadow(), new Forest()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.playLand(player2, 0);

        assertThat(findPermanent(player2, "Boros Signet").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Elves of Deep Shadow").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's artifacts, creatures, and lands enter untapped")
    void controllersArtifactsCreaturesAndLandsEnterUntapped() {
        harness.addToBattlefield(player1, new LoxodonGatekeeper());
        harness.setHand(player1, List.of(new BorosSignet(), new ElvesOfDeepShadow(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Boros Signet").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Elves of Deep Shadow").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's enchantments enter untapped")
    void opponentsEnchantmentsEnterUntapped() {
        harness.addToBattlefield(player1, new LoxodonGatekeeper());
        harness.setHand(player2, List.of(new SuppressionField()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Suppression Field").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's creature tokens enter tapped while their Aura enters untapped")
    void opponentsCreatureTokensEnterTapped() {
        harness.addToBattlefield(player1, new LoxodonGatekeeper());
        harness.addToBattlefield(player2, new ElvesOfDeepShadow());
        harness.setHand(player2, List.of(new FistsOfIronwood()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player2, 0, harness.getPermanentId(player2, "Elves of Deep Shadow"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Fists of Ironwood").isTapped()).isFalse();
        assertThat(findPermanents(player2, "Saproling"))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("A creature enters untapped if Gatekeeper is destroyed before it resolves")
    void creatureEntersUntappedAfterGatekeeperIsDestroyed() {
        harness.addToBattlefield(player1, new LoxodonGatekeeper());
        harness.setHand(player2, List.of(new ElvesOfDeepShadow(), new Putrefy()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player2, 0);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Loxodon Gatekeeper"));
        harness.assertInGraveyard(player1, "Loxodon Gatekeeper");
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Elves of Deep Shadow").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Gatekeeper does not prevent affected permanents from untapping")
    void affectedPermanentsUntapNormally() {
        harness.addToBattlefield(player1, new LoxodonGatekeeper());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player2, 0);
        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();

        harness.performUntapStep(player2);

        assertThat(findPermanent(player2, "Forest").isTapped()).isFalse();
    }
}
