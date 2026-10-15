package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AncientSpider;
import com.github.laxika.magicalvibes.cards.k.KeldonTwilight;
import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.cards.t.TerminalMoraine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaliciousAdvice.class, ManaCylix.class, AncientSpider.class, TerminalMoraine.class,
        KeldonTwilight.class})
class MaliciousAdviceTest extends BaseCardTest {

    @Test
    @DisplayName("Taps exactly X artifacts, creatures, and lands and makes its controller lose X life")
    void tapsMixedPermanentTypesAndLosesXLife() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ManaCylix());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TerminalMoraine());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MaliciousAdvice()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantForX(player1, 0, 3, List.of(artifact.getId(), creature.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("X=0 requires no targets and causes no life loss")
    void xZeroDoesNothing() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MaliciousAdvice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a permanent that is not an artifact, creature, or land")
    void cannotTargetOtherPermanent() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new KeldonTwilight());
        harness.setHand(player1, List.of(new MaliciousAdvice()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Must choose exactly X targets")
    void requiresExactlyXTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new MaliciousAdvice()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 2, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void alreadyTappedOwnTargetStillCausesLifeLoss() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ManaCylix());
        artifact.tap();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MaliciousAdvice()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantForX(player1, 0, 1, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Malicious Advice");
    }

    @Test
    void oneTargetLeavingDoesNotReduceLifeLoss() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ManaCylix());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MaliciousAdvice()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantForX(player1, 0, 2, List.of(artifact.getId(), creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        harness.setGraveyard(player2, List.of(artifact.getCard()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Malicious Advice");
    }

    @Test
    void allTargetsLeavingPreventsLifeLoss() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MaliciousAdvice()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantForX(player1, 0, 1, List.of(creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.setGraveyard(player2, List.of(creature.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Malicious Advice");
    }

    @Test
    void canChooseMoreThanOneHundredTargets() {
        List<Permanent> artifacts = new ArrayList<>();
        List<UUID> targets = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ManaCylix());
            artifacts.add(artifact);
            targets.add(artifact.getId());
        }
        harness.setLife(player1, 200);
        harness.setHand(player1, List.of(new MaliciousAdvice()));
        harness.addMana(player1, ManaColor.BLUE, 102);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantForX(player1, 0, 101, targets);
        harness.passBothPriorities();

        assertThat(artifacts).allMatch(Permanent::isTapped);
        harness.assertLife(player1, 99);
        harness.assertInGraveyard(player1, "Malicious Advice");
    }
}
