package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CloudcrestLake;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TerrainGeneratorTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds {C}")
    void tapForColorlessMana() {
        Permanent generator = addGenerator();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(generator.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts a basic land from hand onto the battlefield tapped")
    void putsBasicLandTapped() {
        Permanent generator = addGenerator();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(generator.isTapped()).isTrue();
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gameData.playerHands.get(player1.getId())).isEmpty();
        Permanent forest = gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Forest)
                .findFirst()
                .orElseThrow();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only basic land cards are offered")
    void onlyOffersBasicLands() {
        addGenerator();
        harness.setHand(player1, List.of(new Forest(), new CloudcrestLake()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.HandChoice choice = (PendingInteraction.HandChoice)
                harness.getGameData().interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Declining the may choice leaves the land in hand")
    void decliningMayChoiceLeavesHandUnchanged() {
        Permanent generator = addGenerator();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(generator.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    private Permanent addGenerator() {
        Permanent generator = new Permanent(new TerrainGenerator());
        generator.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(generator);
        return generator;
    }
}
