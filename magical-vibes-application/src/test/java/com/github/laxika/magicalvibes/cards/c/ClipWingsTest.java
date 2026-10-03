package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.StormriderSpirit;
import com.github.laxika.magicalvibes.cards.t.ThornhideWolves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClipWings.class, StormriderSpirit.class, ThornhideWolves.class})
class ClipWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent sacrifices a creature with flying")
    void eachOpponentSacrificesFlyingCreature() {
        harness.addToBattlefield(player1, flyingCreature("Controller Flyer"));
        harness.addToBattlefield(player2, flyingCreature("Opponent Flyer"));
        harness.addToBattlefield(player2, creature("Ground Creature"));

        castAndResolveClipWings();

        harness.assertOnBattlefield(player1, "Controller Flyer");
        harness.assertNotOnBattlefield(player2, "Opponent Flyer");
        harness.assertOnBattlefield(player2, "Ground Creature");
    }

    @Test
    @DisplayName("An opponent chooses which eligible flying creature to sacrifice")
    void opponentChoosesFlyingCreature() {
        harness.addToBattlefield(player2, flyingCreature("First Flyer"));
        Permanent second = harness.addToBattlefieldAndReturn(player2, flyingCreature("Second Flyer"));

        castAndResolveClipWings();

        GameData gameData = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        harness.assertOnBattlefield(player2, "First Flyer");
        harness.assertNotOnBattlefield(player2, "Second Flyer");
    }

    @Test
    @DisplayName("An opponent without a flying creature sacrifices nothing")
    void noFlyingCreatureIsUnaffected() {
        harness.addToBattlefield(player2, creature("Ground Creature"));

        castAndResolveClipWings();

        harness.assertOnBattlefield(player2, "Ground Creature");
    }

    @Test
    @DisplayName("Hexproof and indestructible do not prevent sacrificing a flying creature")
    void sacrificesProtectedFlyingCreature() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new StormriderSpirit());
        flyer.getGrantedKeywords().addAll(Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE));
        harness.addToBattlefield(player2, new ThornhideWolves());

        castAndResolveClipWings();

        harness.assertNotOnBattlefield(player2, "Stormrider Spirit");
        harness.assertInGraveyard(player2, "Stormrider Spirit");
        harness.assertOnBattlefield(player2, "Thornhide Wolves");
    }

    @Test
    @DisplayName("A creature that gains flying before resolution is eligible for sacrifice")
    void usesFlyingGainedBeforeResolution() {
        Permanent wolves = harness.addToBattlefieldAndReturn(player2, new ThornhideWolves());

        castClipWings();
        wolves.getGrantedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Thornhide Wolves");
        harness.assertInGraveyard(player2, "Thornhide Wolves");
    }

    @Test
    @DisplayName("A creature that loses flying before resolution is not sacrificed")
    void ignoresFlyingLostBeforeResolution() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new StormriderSpirit());

        castClipWings();
        flyer.getRemovedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Stormrider Spirit");
        harness.assertNotInGraveyard(player2, "Stormrider Spirit");
    }

    private void castAndResolveClipWings() {
        prepareClipWings();
        harness.castAndResolveInstant(player1, 0);
    }

    private void castClipWings() {
        prepareClipWings();
        harness.castInstant(player1, 0);
    }

    private void prepareClipWings() {
        harness.setHand(player1, List.of(new ClipWings()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private static Card flyingCreature(String name) {
        Card card = creature(name);
        card.setKeywords(Set.of(Keyword.FLYING));
        return card;
    }

    private static Card creature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
