package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.w.WildwoodTracker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CastleGarenbrig.class, Forest.class, Gingerbrute.class, GoldenEgg.class, WildwoodTracker.class})
class CastleGarenbrigTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped without a Forest")
    void entersTappedWithoutForest() {
        harness.setHand(player1, List.of(new CastleGarenbrig()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Castle Garenbrig").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Forest")
    void entersUntappedWithForest() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new CastleGarenbrig()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Castle Garenbrig").isTapped()).isFalse();
    }

    @Test
    @DisplayName("The first ability adds one green mana")
    void firstAbilityAddsGreenMana() {
        harness.addToBattlefield(player1, new CastleGarenbrig());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = findPermanent(player1, "Castle Garenbrig");
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability adds six creature-restricted green mana")
    void secondAbilityAddsRestrictedMana() {
        harness.addToBattlefield(player1, new CastleGarenbrig());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 1, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.getCreatureSpellOrAbilityMana(ManaColor.GREEN)).isEqualTo(6);
    }

    @Test
    @DisplayName("Restricted mana can cast a creature spell")
    void restrictedManaCanCastCreatureSpell() {
        addCastleAndProduceRestrictedMana();
        harness.setHand(player1, List.of(createCard("Test Creature", CardType.CREATURE, "{G}", CardColor.GREEN)));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Restricted mana cannot cast a noncreature spell")
    void restrictedManaCannotCastNoncreatureSpell() {
        addCastleAndProduceRestrictedMana();
        harness.setHand(player1, List.of(createCard("Test Instant", CardType.INSTANT, "{G}", CardColor.GREEN)));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsForestDoesNotAllowUntappedEntry() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new CastleGarenbrig()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Castle Garenbrig").isTapped()).isTrue();
    }

    @Test
    void tappedForestStillAllowsUntappedEntry() {
        harness.addToBattlefield(player1, new Forest());
        findPermanent(player1, "Forest").tap();
        harness.setHand(player1, List.of(new CastleGarenbrig()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Castle Garenbrig").isTapped()).isFalse();
    }

    @Test
    void restrictedManaCastsRealGreenCreatureAndPreservesRestrictionOnRemainder() {
        addCastleAndProduceRestrictedMana();
        harness.setHand(player1, List.of(new WildwoodTracker()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wildwood Tracker");
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOrAbilityMana(ManaColor.GREEN))
                .isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void restrictedManaPaysForArtifactCreatureAbility() {
        addCastleAndProduceRestrictedMana();
        addCreatureReady(player1, new Gingerbrute());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player1, "Gingerbrute");
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOrAbilityMana(ManaColor.GREEN))
                .isEqualTo(4);
    }

    @Test
    void restrictedManaCannotCastNoncreatureArtifact() {
        addCastleAndProduceRestrictedMana();
        harness.setHand(player1, List.of(new GoldenEgg()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Golden Egg");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void restrictedManaCannotActivateNoncreatureArtifactAbility() {
        addCastleAndProduceRestrictedMana();
        harness.addToBattlefield(player1, new GoldenEgg());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Golden Egg");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void secondAbilityRequiresTwoGreenMana() {
        harness.addToBattlefield(player1, new CastleGarenbrig());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Castle Garenbrig").isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOrAbilityMana(ManaColor.GREEN))
                .isZero();
    }
    private void addCastleAndProduceRestrictedMana() {
        harness.addToBattlefield(player1, new CastleGarenbrig());
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 0, 1, null, null);
    }

    private static Card createCard(String name, CardType type, String manaCost, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost(manaCost);
        card.setColor(color);
        if (type == CardType.CREATURE) {
            card.setPower(2);
            card.setToughness(2);
        }
        return card;
    }
}
