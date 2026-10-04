package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SlipperyBogle;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FableOfWolfAndOwl.class, AirElemental.class, GrizzlyBears.class, HillGiant.class, SlipperyBogle.class})
class FableOfWolfAndOwlTest extends BaseCardTest {

    private long tokenCount(UUID playerId, String name) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .count();
    }

    @Test
    @DisplayName("Casting a green spell may create a 2/2 green Wolf token")
    void greenSpellCreatesWolf() {
        harness.addToBattlefield(player1, new FableOfWolfAndOwl());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(tokenCount(player1.getId(), "Wolf")).isEqualTo(1);
        assertThat(tokenCount(player1.getId(), "Bird")).isZero();
    }

    @Test
    @DisplayName("Casting a blue spell may create a 1/1 blue flying Bird token")
    void blueSpellCreatesBird() {
        harness.addToBattlefield(player1, new FableOfWolfAndOwl());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(tokenCount(player1.getId(), "Bird")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bird"))
                .anyMatch(p -> p.getCard().getKeywords().contains(Keyword.FLYING))).isTrue();
    }

    @Test
    @DisplayName("Declining the green trigger creates no token")
    void declineCreatesNoToken() {
        harness.addToBattlefield(player1, new FableOfWolfAndOwl());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(tokenCount(player1.getId(), "Wolf")).isZero();
    }

    @Test
    @DisplayName("Casting a red spell triggers neither ability")
    void redSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new FableOfWolfAndOwl());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(tokenCount(player1.getId(), "Wolf")).isZero();
        assertThat(tokenCount(player1.getId(), "Bird")).isZero();
    }

    @Test
    @DisplayName("The token choice waits for resolution and the trigger exists before the choice")
    void tokenChoiceWaitsForResolution() {
        harness.addToBattlefield(player1, new FableOfWolfAndOwl());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Wolf");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Wolf");
    }

    @ParameterizedTest
    @CsvSource({"GREEN,true,true", "BLUE,true,true", "GREEN,true,false", "BLUE,false,true", "GREEN,false,false"})
    @DisplayName("A green-blue hybrid spell triggers both abilities with independent choices")
    void hybridSpellTriggersBothAbilities(ManaColor payment, boolean createWolf, boolean createBird) {
        harness.addToBattlefield(player1, new FableOfWolfAndOwl());
        harness.setHand(player1, List.of(new SlipperyBogle()));
        harness.addMana(player1, payment, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(3);
        for (int i = 0; i < 2; i++) {
            harness.passBothPriorities();
            var choice = gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.playerId()).isEqualTo(player1.getId());
            boolean wolf = choice.description().contains("Wolf creature");
            harness.handleMayAbilityChosen(player1, wolf ? createWolf : createBird);
        }

        assertThat(tokenCount(player1.getId(), "Wolf")).isEqualTo(createWolf ? 1 : 0);
        assertThat(tokenCount(player1.getId(), "Bird")).isEqualTo(createBird ? 1 : 0);
        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .forEach(p -> {
                    var token = p.getCard();
                    boolean wolf = token.getName().equals("Wolf");
                    assertThat(token.getPower()).isEqualTo(wolf ? 2 : 1);
                    assertThat(token.getToughness()).isEqualTo(wolf ? 2 : 1);
                    assertThat(token.getColors()).containsExactly(wolf ? CardColor.GREEN : CardColor.BLUE);
                    assertThat(token.getType()).isEqualTo(CardType.CREATURE);
                    assertThat(token.getSubtypes()).containsExactly(wolf ? CardSubtype.WOLF : CardSubtype.BIRD);
                    assertThat(token.getKeywords().contains(Keyword.FLYING)).isEqualTo(!wolf);
                });
        harness.assertNotOnBattlefield(player1, "Slippery Bogle");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Slippery Bogle");
    }

    @Test
    @DisplayName("An opponent's green-blue spell does not trigger Fable")
    void opponentsSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new FableOfWolfAndOwl());
        gd.activePlayerId = player2.getId();
        harness.setHand(player2, List.of(new SlipperyBogle()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Wolf");
        harness.assertNotOnBattlefield(player1, "Bird");
    }

    @Test
    @DisplayName("Fable does not trigger on its own casting")
    void doesNotTriggerOnItsOwnCasting() {
        harness.setHand(player1, List.of(new FableOfWolfAndOwl()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castEnchantment(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fable of Wolf and Owl");
        harness.assertNotOnBattlefield(player1, "Wolf");
        harness.assertNotOnBattlefield(player1, "Bird");
    }
}
