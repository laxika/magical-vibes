package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArlinnKordTest extends BaseCardTest {

    @Nested
    @DisplayName("Front face +1: up to one creature +2/+2 vigilance haste")
    class FrontPlusOne {

        @Test
        @DisplayName("Pumps target creature and grants vigilance and haste")
        void pumpsAndGrantsKeywords() {
            Permanent arlinn = addFrontFace(player1, 3);
            Permanent target = addCreature(player1, "GrizzlyBears", 2, 2);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(arlinn);
            harness.activateAbility(player1, idx, 0, target.getId(), null);
            harness.passBothPriorities();

            assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
            assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
            assertThat(target.hasKeyword(Keyword.VIGILANCE)).isTrue();
            assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        }

        @Test
        @DisplayName("Can activate with no target")
        void canActivateWithNoTarget() {
            Permanent arlinn = addFrontFace(player1, 3);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(arlinn);
            harness.activateAbility(player1, idx, 0, null, null);
            harness.passBothPriorities();

            assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Front face 0: Wolf token and transform")
    class FrontZero {

        @Test
        @DisplayName("Creates a 2/2 green Wolf and transforms")
        void createsWolfAndTransforms() {
            Permanent arlinn = addFrontFace(player1, 3);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(arlinn);
            harness.activateAbility(player1, idx, 1, null, null);
            harness.passBothPriorities();

            assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
            assertThat(arlinn.isTransformed()).isTrue();
            assertThat(arlinn.getCard().getName()).isEqualTo("Arlinn, Embraced by the Moon");

            Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Wolf"))
                    .findFirst().orElseThrow();
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.WOLF);
        }
    }

    @Nested
    @DisplayName("Back face +1: team pump and trample")
    class BackPlusOne {

        @Test
        @DisplayName("Gives own creatures +1/+1 and trample")
        void pumpsOwnCreatures() {
            Permanent arlinn = addTransformedBackFace(player1, 3);
            Permanent creature = addCreature(player1, "GrizzlyBears", 2, 2);
            Permanent opp = addCreature(player2, "EliteVanguard", 2, 1);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(arlinn);
            harness.activateAbility(player1, idx, 0, null, null);
            harness.passBothPriorities();

            assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
            assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
            assertThat(gqs.getEffectivePower(gd, opp)).isEqualTo(2);
            assertThat(opp.hasKeyword(Keyword.TRAMPLE)).isFalse();
        }
    }

    @Nested
    @DisplayName("Back face -1: damage and transform back")
    class BackMinusOne {

        @Test
        @DisplayName("Deals 3 damage to a player and transforms to front face")
        void damagesPlayerAndTransformsBack() {
            Permanent arlinn = addTransformedBackFace(player1, 3);
            int lifeBefore = gd.playerLifeTotals.get(player2.getId());

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(arlinn);
            harness.activateAbility(player1, idx, 1, null, player2.getId());
            harness.passBothPriorities();

            assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
            assertThat(arlinn.isTransformed()).isFalse();
            assertThat(arlinn.getCard().getName()).isEqualTo("Arlinn Kord");
        }

        @Test
        @DisplayName("Deals 3 damage to a creature")
        void damagesCreature() {
            Permanent arlinn = addTransformedBackFace(player1, 3);
            Permanent target = addCreature(player2, "GrizzlyBears", 2, 2);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(arlinn);
            harness.activateAbility(player1, idx, 1, null, target.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(arlinn.isTransformed()).isFalse();
        }
    }

    @Nested
    @DisplayName("Back face -6: emblem")
    class BackMinusSix {

        @Test
        @DisplayName("Creates emblem with haste and tap-for-power damage")
        void createsEmblem() {
            Permanent arlinn = addTransformedBackFace(player1, 6);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(arlinn);
            harness.activateAbility(player1, idx, 2, null, null);
            harness.passBothPriorities();

            assertThat(gd.emblems).hasSize(1);
            Emblem emblem = gd.emblems.getFirst();
            assertThat(emblem.controllerId()).isEqualTo(player1.getId());
            assertThat(emblem.staticEffects()).hasSize(2);
            assertThat(emblem.staticEffects().get(0)).isInstanceOf(GrantKeywordEffect.class);
            GrantKeywordEffect haste = (GrantKeywordEffect) emblem.staticEffects().get(0);
            assertThat(haste.scope()).isEqualTo(GrantScope.OWN_PERMANENTS);
            assertThat(haste.keywords()).contains(Keyword.HASTE);
            assertThat(emblem.staticEffects().get(1)).isInstanceOf(GrantActivatedAbilityEffect.class);
            GrantActivatedAbilityEffect grant = (GrantActivatedAbilityEffect) emblem.staticEffects().get(1);
            assertThat(grant.scope()).isEqualTo(GrantScope.OWN_PERMANENTS);
            assertThat(grant.ability().isRequiresTap()).isTrue();
        }

        @Test
        @DisplayName("Emblem grants haste and lets a creature deal power damage")
        void emblemGrantsHasteAndTapDamage() {
            Permanent arlinn = addTransformedBackFace(player1, 6);
            Permanent creature = addCreature(player1, "GrizzlyBears", 2, 2);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(arlinn);
            harness.activateAbility(player1, idx, 2, null, null);
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
            assertThat(gs.getEffectiveActivatedAbilities(gd, creature)).hasSize(1);

            int creatureIdx = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
            int lifeBefore = gd.playerLifeTotals.get(player2.getId());
            harness.activateAbility(player1, creatureIdx, null, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
            assertThat(creature.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Cannot activate -6 with insufficient loyalty")
        void cannotActivateWithInsufficientLoyalty() {
            Permanent arlinn = addTransformedBackFace(player1, 5);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(arlinn);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, 2, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    private Permanent addFrontFace(Player player, int loyalty) {
        ArlinnKord card = new ArlinnKord();
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addTransformedBackFace(Player player, int loyalty) {
        ArlinnKord card = new ArlinnKord();
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        perm.setTransformed(true);
        perm.setCard(card.getBackFaceCard());
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addCreature(Player player, String name, int power, int toughness) {
        Card card = createCreatureCard(name, power, toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createCreatureCard(String name, int power, int toughness) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
