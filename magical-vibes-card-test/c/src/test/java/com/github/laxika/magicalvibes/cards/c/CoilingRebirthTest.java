package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.cards.b.BaylenTheHaymaker;
import com.github.laxika.magicalvibes.cards.d.DaggerfangDuo;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoilingRebirth.class, BarkformHarvester.class, BaylenTheHaymaker.class, DaggerfangDuo.class})
class CoilingRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature and creates a 1/1 copy when Gift is promised")
    void returnsCreatureAndCreatesCopyWhenGiftPromised() {
        Card creature = new BarkformHarvester();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CoilingRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castSorceryWithGift(player1, 0, List.of(creature.getId()), true);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        Permanent token = gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(gameData.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
    }

    @Test
    @DisplayName("Returns a creature without creating a copy when Gift is not promised")
    void returnsCreatureWithoutCopyWhenGiftNotPromised() {
        Card creature = new BarkformHarvester();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CoilingRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castSorceryWithGift(player1, 0, List.of(creature.getId()), false);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gameData.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    @DisplayName("Does not copy the returned creature when it is legendary")
    void doesNotCopyLegendaryCreature() {
        Card creature = new BaylenTheHaymaker();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CoilingRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorceryWithGift(player1, 0, List.of(creature.getId()), true);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNoncreatureCard() {
        Card noncreature = new CoilingRebirth();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setHand(player1, List.of(new CoilingRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorceryWithGift(player1, 0, List.of(noncreature.getId()), false))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new BarkformHarvester();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new CoilingRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorceryWithGift(player1, 0, List.of(creature.getId()), true))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("An illegal target prevents the gift draw and all other effects")
    void doesNothingWhenTargetLeavesGraveyard() {
        Card creature = new BarkformHarvester();
        harness.addToBattlefield(player1, new BarkformHarvester());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CoilingRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castSorceryWithGift(player1, 0, List.of(creature.getId()), true);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerDecks.get(player1.getId())).contains(creature);
        harness.assertInGraveyard(player1, "Coiling Rebirth");
    }

    @Test
    @DisplayName("The 1/1 token retains the returned creature's activated ability")
    void tokenRetainsActivatedAbility() {
        Card creature = new BarkformHarvester();
        Card spell = new CoilingRebirth();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorceryWithGift(player1, 0, List.of(creature.getId()), true);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbilityWithGraveyardTargets(player1, tokenIndex, 0, List.of(spell.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Coiling Rebirth");
        assertThat(gd.playerDecks.get(player1.getId())).last().isEqualTo(spell);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The returned creature and its token both trigger their enters abilities after the spell resolves")
    void bothEntersAbilitiesWaitForSpellToResolve() {
        Card creature = new DaggerfangDuo();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new BarkformHarvester(), new BarkformHarvester(),
                new BarkformHarvester(), new BarkformHarvester(), new BarkformHarvester()));
        harness.setHand(player1, List.of(new CoilingRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorceryWithGift(player1, 0, List.of(creature.getId()), true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Coiling Rebirth");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
    }
}
