package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KazanduNectarpot;
import com.github.laxika.magicalvibes.cards.t.TajuruBlightblade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AshayaSoulOfTheWild.class, Forest.class, GrizzlyBears.class,
        KazanduNectarpot.class, TajuruBlightblade.class})
class AshayaSoulOfTheWildTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness track the number of lands, including creatures made into lands")
    void powerAndToughnessTrackLands() {
        Permanent ashaya = addCreatureReady(player1, new AshayaSoulOfTheWild());
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.isLand(gd, ashaya)).isTrue();
        assertThat(gqs.isLand(gd, bears)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ashaya)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ashaya)).isEqualTo(3);

        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, ashaya)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ashaya)).isEqualTo(4);
    }

    @Test
    @DisplayName("Nontoken creatures you control become Forest lands and can produce green mana")
    void ownNontokenCreaturesBecomeForestLands() {
        addCreatureReady(player1, new AshayaSoulOfTheWild());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCreature());

        assertThat(gqs.isLand(gd, bears)).isTrue();
        assertThat(gqs.effectiveBasicLandTypes(gd, bears))
                .contains(CardSubtype.FOREST);
        assertThat(gqs.isLand(gd, opponentBears)).isFalse();
        assertThat(gqs.isLand(gd, token)).isFalse();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bears), null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(bears.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(token), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void ashayaCanTapForGreenManaItself() {
        Permanent ashaya = addCreatureReady(player1, new AshayaSoulOfTheWild());

        harness.activateAbility(player1, 0, null, null);

        assertThat(ashaya.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void becomingALandDoesNotBypassSummoningSickness() {
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());
        harness.addToBattlefield(player1, new TajuruBlightblade());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void forestTypeAndManaAbilityDisappearWhenAshayaLeaves() {
        Permanent ashaya = addCreatureReady(player1, new AshayaSoulOfTheWild());
        Permanent creature = addCreatureReady(player1, new TajuruBlightblade());
        assertThat(gqs.isLand(gd, creature)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, ashaya));

        assertThat(gqs.isLand(gd, creature)).isFalse();
        assertThat(gqs.effectiveBasicLandTypes(gd, creature)).doesNotContain(CardSubtype.FOREST);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void nontokenCreatureEntersAsALandAndTriggersLandfall() {
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());
        harness.addToBattlefield(player1, new KazanduNectarpot());
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player1, new TajuruBlightblade());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    void ashayaEnteringTriggersLandfallButExistingCreaturesBecomingLandsDoNot() {
        harness.addToBattlefield(player1, new KazanduNectarpot());
        harness.addToBattlefield(player1, new TajuruBlightblade());
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player1, new AshayaSoulOfTheWild());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    void powerAndToughnessInHandCountOnlyLandsOnTheBattlefield() {
        AshayaSoulOfTheWild ashaya = new AshayaSoulOfTheWild();
        harness.setHand(player1, java.util.List.of(ashaya));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new TajuruBlightblade());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectiveCardPower(gd, ashaya)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, ashaya)).isEqualTo(1);
    }

    @Test
    void powerAndToughnessInGraveyardTrackOwnersLands() {
        AshayaSoulOfTheWild ashaya = new AshayaSoulOfTheWild();
        gd.playerGraveyards.get(player1.getId()).add(ashaya);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectiveCardPower(gd, ashaya)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, ashaya)).isEqualTo(1);

        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectiveCardPower(gd, ashaya)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, ashaya)).isEqualTo(2);
    }

    private Card tokenCreature() {
        Card card = new Card();
        card.setName("Token Creature");
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
