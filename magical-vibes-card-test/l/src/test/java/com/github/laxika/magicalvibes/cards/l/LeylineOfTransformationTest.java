package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CautiousSurvivor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeylineOfTransformation.class, GrizzlyBears.class, CautiousSurvivor.class})
class LeylineOfTransformationTest extends BaseCardTest {

    @Test
    @DisplayName("Leyline in the opening hand may begin the game on the battlefield")
    void leylineInOpeningHandMayStartOnBattlefield() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfTransformation()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), true);
        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();
        openingHarness.handleListChoice(openingHarness.getPlayer1(), CardSubtype.GOBLIN.name());

        Permanent leyline = openingHarness.getGameData().playerBattlefields
                .get(openingHarness.getPlayer1().getId()).stream()
                .filter(p -> p.getCard().getName().equals("Leyline of Transformation"))
                .findFirst()
                .orElseThrow();
        assertThat(leyline.getChosenSubtype()).isEqualTo(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Leyline grants the chosen type to own creatures and creature cards outside the battlefield")
    void grantsChosenTypeToOwnCreaturesAndCreatureCardsOutsideBattlefield() {
        Permanent battlefieldBearPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card handBear = creature("Hand Bear", CardSubtype.BEAR);
        Card graveyardBear = creature("Graveyard Bear", CardSubtype.BEAR);
        Card opponentBear = creature("Opponent Bear", CardSubtype.BEAR);
        harness.setHand(player1, List.of(new LeylineOfTransformation(), handBear));
        gd.playerGraveyards.get(player1.getId()).add(graveyardBear);
        gd.playerHands.get(player2.getId()).add(opponentBear);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.computeStaticBonus(gd, battlefieldBearPermanent).grantedSubtypes())
                .contains(CardSubtype.GOBLIN);
        assertThat(gqs.cardHasSubtype(handBear, CardSubtype.GOBLIN, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(graveyardBear, CardSubtype.GOBLIN, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(opponentBear, CardSubtype.GOBLIN, gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Declining the opening-hand option keeps Leyline in hand")
    void mayDeclineOpeningHandOption() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfTransformation()));
        openingHarness.skipMulligan();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), false);

        openingHarness.assertInHand(openingHarness.getPlayer1(), "Leyline of Transformation");
        openingHarness.assertNotOnBattlefield(openingHarness.getPlayer1(), "Leyline of Transformation");
        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Library and exile creatures gain the type, but noncreature cards do not")
    void affectsLibraryAndExileButNotNoncreatures() {
        CautiousSurvivor libraryCreature = new CautiousSurvivor();
        CautiousSurvivor exiledCreature = new CautiousSurvivor();
        LeylineOfTransformation noncreature = new LeylineOfTransformation();
        harness.setLibrary(player1, List.of(libraryCreature));
        harness.setExile(player1, List.of(exiledCreature));
        harness.setHand(player1, List.of(new LeylineOfTransformation(), noncreature));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.cardHasSubtype(libraryCreature, CardSubtype.GOBLIN, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(exiledCreature, CardSubtype.GOBLIN, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(libraryCreature, CardSubtype.ELF, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(noncreature, CardSubtype.GOBLIN, gd, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Chosen type applies to controlled creatures and preserves their original types")
    void affectsOnlyControlledCreaturesAndPreservesTypes() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CautiousSurvivor());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());
        harness.setHand(player1, List.of(new LeylineOfTransformation()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.computeStaticBonus(gd, ownCreature).grantedSubtypes()).contains(CardSubtype.GOBLIN);
        assertThat(gqs.hasEffectiveSubtype(gd, ownCreature, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, ownCreature, CardSubtype.SURVIVOR)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, opposingCreature).grantedSubtypes()).doesNotContain(CardSubtype.GOBLIN);
        assertThat(gqs.computeStaticBonus(gd, findPermanent(player1, "Leyline of Transformation"))
                .grantedSubtypes()).doesNotContain(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Multiple Leylines grant independent types and stop granting when they leave")
    void multipleLeylinesGrantIndependentTypes() {
        CautiousSurvivor handCreature = new CautiousSurvivor();
        harness.setHand(player1, List.of(new LeylineOfTransformation(), new LeylineOfTransformation(), handCreature));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.ZOMBIE.name());

        assertThat(gqs.cardHasSubtype(handCreature, CardSubtype.GOBLIN, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(handCreature, CardSubtype.ZOMBIE, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(handCreature, CardSubtype.ELF, gd, player1.getId())).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(findPermanents(player1, "Leyline of Transformation").getFirst());

        assertThat(gqs.cardHasSubtype(handCreature, CardSubtype.GOBLIN, gd, player1.getId())).isFalse();
        assertThat(gqs.cardHasSubtype(handCreature, CardSubtype.ZOMBIE, gd, player1.getId())).isTrue();
    }
    @Test
    @DisplayName("Creature spells gain the chosen type on the stack and retain it after resolving")
    void creatureSpellGainsChosenType() {
        CautiousSurvivor creature = new CautiousSurvivor();
        harness.setHand(player1, List.of(new LeylineOfTransformation(), creature));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.cardHasSubtype(gd.stack.getFirst().getCard(), CardSubtype.GOBLIN,
                gd, gd.stack.getFirst().getControllerId())).isTrue();

        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, findPermanent(player1, "Cautious Survivor"),
                CardSubtype.GOBLIN)).isTrue();
    }
    private static Card creature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
