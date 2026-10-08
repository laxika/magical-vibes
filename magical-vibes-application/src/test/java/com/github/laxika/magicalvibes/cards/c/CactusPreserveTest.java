package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BondersEnclave;
import com.github.laxika.magicalvibes.cards.y.YumaProudProtector;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CactusPreserve.class, YumaProudProtector.class, BondersEnclave.class, CommandTower.class})
class CactusPreserveTest extends BaseCardTest {

    @Test
    @DisplayName("Cactus Preserve enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new CactusPreserve()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Cactus Preserve").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability can add colorless mana that a land you control could produce")
    void addsColorlessManaFromControlledLand() {
        addCactusReady(player1);
        harness.addToBattlefield(player1, new BondersEnclave());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Animation uses the greatest mana value among the controller's commanders")
    void animationUsesGreatestCommanderManaValue() {
        prepareCommander();
        Permanent cactus = addCactusReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        if (gd.interaction.activeInteraction() instanceof
                com.github.laxika.magicalvibes.model.PendingInteraction.CommanderReturnChoice) {
            harness.handleMayAbilityChosen(player1, false);
        }
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, cactus)).isTrue();
        assertThat(gqs.isCreature(gd, cactus)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cactus)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, cactus)).isEqualTo(8);
        assertThat(gqs.getEffectiveColors(gd, cactus)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, cactus)).contains(CardSubtype.PLANT);
        assertThat(gqs.hasKeyword(gd, cactus, Keyword.REACH)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, cactus)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"hand", "library", "graveyard", "exile"})
    void animationCountsCommanderOutsideCommandZoneAndBattlefield(String zone) {
        Card commander = prepareCommander();
        gd.playerCommandZones.get(player1.getId()).clear();
        switch (zone) {
            case "hand" -> harness.setHand(player1, List.of(commander));
            case "library" -> harness.setLibrary(player1, List.of(commander));
            case "graveyard" -> harness.setGraveyard(player1, List.of(commander));
            case "exile" -> harness.setExile(player1, List.of(commander));
            default -> throw new IllegalArgumentException(zone);
        }
        gd.commanderReturnCandidates.remove(commander.getId());
        Permanent cactus = addCactusReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cactus Preserve");
        assertThat(gqs.getEffectivePower(gd, cactus)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, cactus)).isEqualTo(8);
    }

    @Test
    void animationDoesNotRecalculateSizeWhenCommanderTurnsFaceDown() {
        Card commander = prepareCommander();
        gd.playerCommandZones.get(player1.getId()).clear();
        Permanent cactus = addCactusReady(player1);
        Permanent commanderPermanent = harness.addToBattlefieldAndReturn(player1, commander);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, cactus)).isEqualTo(8);

        commanderPermanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        assertThat(gqs.getEffectivePower(gd, cactus)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, cactus)).isEqualTo(8);
    }

    @Test
    void animationWithoutCommanderDiesAsZeroToughnessCreature() {
        addCactusReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cactus Preserve");
        harness.assertInGraveyard(player1, "Cactus Preserve");
    }

    @Test
    void manaAbilityIgnoresOpponentsLandsAndCannotBootstrapItself() {
        addCactusReady(player1);
        harness.addToBattlefield(player2, new BondersEnclave());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void manaAbilityCanUseTypeProducedByTappedLand() {
        addCactusReady(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new BondersEnclave());
        land.tap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void manaAbilityCanChooseColoredManaFromControlledLand() {
        prepareCommander();
        addCactusReady(player1);
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new CommandTower());
        tower.tap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    private Permanent addCactusReady(Player player) {
        return addCreatureReady(player, new CactusPreserve());
    }

    private Card prepareCommander() {
        Card commander = new YumaProudProtector();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        return commander;
    }
}
