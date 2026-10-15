package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AnointerPriest;
import com.github.laxika.magicalvibes.cards.a.AnointedProcession;
import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VizierOfManyFaces.class, GrizzlyBears.class, Colossapede.class,
        AnointerPriest.class, AnointedProcession.class})
class VizierOfManyFacesTest extends BaseCardTest {

    private Permanent enteredVizier() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Vizier of Many Faces"))
                .findFirst().orElse(null);
    }

    @Test
    @DisplayName("Hard-cast copies a creature without the embalm transformation")
    void hardCastCopiesWithoutEmbalmTransformation() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new VizierOfManyFaces(), "{2}{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        Permanent vizier = enteredVizier();
        assertThat(vizier).isNotNull();
        // It's a straight copy of Grizzly Bears: keeps the copied creature's color, cost, and types.
        assertThat(vizier.getCard().getPower()).isEqualTo(2);
        assertThat(vizier.getCard().getToughness()).isEqualTo(2);
        assertThat(vizier.getCard().getColor()).isNotEqualTo(CardColor.WHITE);
        assertThat(vizier.getCard().getSubtypes()).contains(CardSubtype.BEAR).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(vizier.getCard().getManaCost()).isNotEmpty();
    }

    @Test
    @DisplayName("Embalmed token enters as a copy that is white, a Zombie, and has no mana cost")
    void embalmTokenCopyGetsEmbalmTransformation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new VizierOfManyFaces()));
        harness.addMana(player1, ManaColor.BLUE, 5); // pays {3}{U}{U}

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve Embalm → token's copy-on-enter may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        Permanent token = enteredVizier();
        assertThat(token).isNotNull();
        assertThat(token.getCard().isToken()).isTrue();
        // Copied Grizzly Bears' body...
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        // ...but transformed by the embalm exception on the final copy.
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getColors()).contains(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE, CardSubtype.BEAR);
        assertThat(token.getCard().getManaCost()).isEmpty();
    }

    @Test
    @DisplayName("Embalmed token declining to copy enters as a 0/0 and dies")
    void embalmTokenDiesWhenDecliningToCopy() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new VizierOfManyFaces()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false); // decline → enters as a white Zombie 0/0

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getOriginalCard().getName().equals("Vizier of Many Faces"));
    }

    @Test
    void hardCastDiesWithoutCreaturesToCopy() {
        harness.castFromHand(player1, new VizierOfManyFaces(), "{2}{U}{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vizier of Many Faces");
        harness.assertInGraveyard(player1, "Vizier of Many Faces");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void hardCastDiesWhenDecliningToCopy() {
        harness.addToBattlefield(player2, new Colossapede());
        harness.castFromHand(player1, new VizierOfManyFaces(), "{2}{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Vizier of Many Faces");
        harness.assertInGraveyard(player1, "Vizier of Many Faces");
    }

    @Test
    void embalmExilesSourceAsCostAndDiesWithoutCreaturesToCopy() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new VizierOfManyFaces()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Vizier of Many Faces");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Vizier of Many Faces"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vizier of Many Faces");
        harness.assertNotInGraveyard(player1, "Vizier of Many Faces");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void embalmCannotBeActivatedDuringCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setGraveyard(player1, List.of(new VizierOfManyFaces()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Vizier of Many Faces");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void embalmedCopyUsesCopiedTriggeredAbilityOnItsOwnEntry() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new AnointerPriest());
        harness.setGraveyard(player1, List.of(new VizierOfManyFaces()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Anointer Priest"));
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(enteredVizier().getCard().getSubtypes())
                .contains(CardSubtype.ZOMBIE, CardSubtype.HUMAN, CardSubtype.CLERIC);
    }

    @Test
    void anointedProcessionCreatesTwoEmbalmedCopies() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new AnointedProcession());
        harness.addToBattlefield(player2, new Colossapede());
        harness.setGraveyard(player1, List.of(new VizierOfManyFaces()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        UUID colossapedeId = harness.getPermanentId(player2, "Colossapede");
        for (int choice = 0; choice < 2; choice++) {
            if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
                harness.handleMayAbilityChosen(player1, true);
                harness.handlePermanentChosen(player1, colossapedeId);
            }
        }

        List<Permanent> copies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(copies).hasSize(2);
        assertThat(copies).allSatisfy(p -> {
            assertThat(p.getCard().getName()).isEqualTo("Colossapede");
            assertThat(p.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(p.getCard().getManaCost()).isEmpty();
            assertThat(p.getCard().getSubtypes()).contains(CardSubtype.INSECT, CardSubtype.ZOMBIE);
        });
    }
}
