package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoldBiochemist;
import com.github.laxika.magicalvibes.cards.h.HydraulicHelper;
import com.github.laxika.magicalvibes.cards.k.KreeSentinel;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShangChiMasterOfKungFu.class, HydraulicHelper.class, BoldBiochemist.class,
        KreeSentinel.class, SurveillanceRoom.class})
class ShangChiMasterOfKungFuTest extends BaseCardTest {

    @Test
    void activatesSummoningSickCreatureAbilitiesAndAddsAnyChosenColor() {
        harness.addToBattlefield(player1, new ShangChiMasterOfKungFu());
        harness.addToBattlefield(player1, createAbilitySource(CardType.CREATURE, CardColor.BLUE));

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getCreatureAbilityOnlyMana(ManaColor.BLUE)).isEqualTo(2);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(pool.getCreatureAbilityOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void restrictedManaCannotActivateNoncreatureSourceAbilities() {
        harness.addToBattlefield(player1, new ShangChiMasterOfKungFu());
        harness.addToBattlefield(player1, createAbilitySource(CardType.ARTIFACT, CardColor.BLUE));

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureAbilityOnlyMana(ManaColor.BLUE))
                .isEqualTo(2);
    }

    @Test
    void summoningSickCreatureCanPayTapCost() {
        harness.addToBattlefield(player1, new ShangChiMasterOfKungFu());
        Permanent helper = harness.addToBattlefieldAndReturn(player1, new HydraulicHelper());

        harness.activateAbility(player1, 1, null, null);

        assertThat(helper.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactSpellOrAbilityOnlyMana(ManaColor.BLUE))
                .isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotHelpOpponentsSummoningSickCreatures() {
        harness.addToBattlefield(player1, new ShangChiMasterOfKungFu());
        harness.addToBattlefield(player2, new HydraulicHelper());
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void hastePermissionEndsWhenShangChiLeavesBattlefield() {
        harness.addToBattlefield(player1, new ShangChiMasterOfKungFu());
        harness.addToBattlefield(player1, new HydraulicHelper());
        gd.playerBattlefields.get(player1.getId()).removeFirst();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void permissionDoesNotAllowSummoningSickCreaturesToAttack() {
        Permanent shangChi = harness.addToBattlefieldAndReturn(player1, new ShangChiMasterOfKungFu());
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new KreeSentinel());

        assertThat(als.canAttack(gd, shangChi, player1.getId())).isFalse();
        assertThat(als.canAttack(gd, sentinel, player1.getId())).isFalse();
    }

    @Test
    void restrictedManaPaysForRealCreatureAbility() {
        harness.addToBattlefield(player1, new ShangChiMasterOfKungFu());
        harness.enterBattlefieldAndReturn(player1, new BoldBiochemist());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new KreeSentinel(), new KreeSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureAbilityOnlyManaTotal()).isZero();
    }

    @Test
    void restrictedManaCannotPayForLandAbility() {
        harness.addToBattlefield(player1, new ShangChiMasterOfKungFu());
        harness.addToBattlefield(player1, new SurveillanceRoom());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureAbilityOnlyMana(ManaColor.BLUE))
                .isEqualTo(2);
    }

    @Test
    void restrictedManaCannotCastCreatureSpell() {
        harness.addToBattlefield(player1, new ShangChiMasterOfKungFu());
        harness.setHand(player1, List.of(new HydraulicHelper()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Hydraulic Helper");
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureAbilityOnlyMana(ManaColor.BLUE))
                .isEqualTo(2);
    }

    @Test
    void restrictedManaCanPayForCreatureLandcyclingFromHand() {
        harness.addToBattlefield(player1, new ShangChiMasterOfKungFu());
        harness.setHand(player1, List.of(new KreeSentinel()));
        harness.setLibrary(player1, List.of(new KreeSentinel()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kree Sentinel");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureAbilityOnlyManaTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void addsTwoManaOfOneChosenColorWithoutUsingStack(ManaColor color) {
        Permanent shangChi = harness.addToBattlefieldAndReturn(player1, new ShangChiMasterOfKungFu());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(shangChi.isTapped()).isTrue();
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getCreatureAbilityOnlyMana(color)).isEqualTo(2);
        assertThat(pool.getCreatureAbilityOnlyManaTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private static Card createAbilitySource(CardType type, CardColor color) {
        Card card = new Card();
        card.setName(type == CardType.CREATURE ? "Blue Creature" : "Blue Artifact");
        card.setType(type);
        card.setManaCost("{2}");
        card.setColor(color);
        if (type == CardType.CREATURE) {
            card.setPower(2);
            card.setToughness(2);
        }
        card.addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new GainLifeEffect(1)),
                "{U}: You gain 1 life."
        ));
        return card;
    }
}
