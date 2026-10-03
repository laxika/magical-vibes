package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DismissIntoDream.class, SerraAngel.class, Shock.class, RodOfRuin.class})
class DismissIntoDreamTest extends BaseCardTest {

    @Test
    @DisplayName("Each creature an opponent controls is an Illusion in addition to its other types")
    void opponentCreaturesBecomeIllusions() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.addToBattlefield(player1, new DismissIntoDream());

        Permanent bears = findPermanent(player2, "Serra Angel");

        assertThat(gqs.computeStaticBonus(gd, bears).grantedSubtypes()).contains(CardSubtype.ILLUSION);
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.ANGEL)).isTrue();
    }

    @Test
    @DisplayName("Creatures its controller controls are unaffected")
    void ownCreaturesAreNotIllusions() {
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player1, new DismissIntoDream());

        Permanent bears = findPermanent(player1, "Serra Angel");

        assertThat(gqs.computeStaticBonus(gd, bears).grantedSubtypes()).doesNotContain(CardSubtype.ILLUSION);
    }

    @Test
    @DisplayName("Targeting an opponent's creature with a spell sacrifices it")
    void targetedOpponentCreatureIsSacrificed() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.addToBattlefield(player1, new DismissIntoDream());

        Permanent bears = findPermanent(player2, "Serra Angel");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Serra Angel"));
    }

    @Test
    @DisplayName("Targeting your own creature does not sacrifice it")
    void ownTargetedCreatureSurvives() {
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player1, new DismissIntoDream());

        Permanent angel = findPermanent(player1, "Serra Angel");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, angel.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Serra Angel")).isNotNull();
    }

    @Test
    void activatedAbilitySacrificesCreatureBeforeDealingDamage() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.addToBattlefield(player1, new DismissIntoDream());
        harness.addToBattlefield(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, angel.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentTargetingTheirOwnCreatureStillSacrificesIt() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.addToBattlefield(player1, new DismissIntoDream());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, angel.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    void cloakedCreatureHasAndTriggersGrantedSacrificeAbility() {
        Permanent cloaked = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        cloaked.setFaceDownAsCloaked();
        harness.addToBattlefield(player1, new DismissIntoDream());
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 1, null, cloaked.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(cloaked);
        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }
}
