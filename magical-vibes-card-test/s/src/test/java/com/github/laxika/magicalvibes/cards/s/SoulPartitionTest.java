package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.ArgothSanctumOfNature;
import com.github.laxika.magicalvibes.cards.t.TitaniaVoiceOfGaea;
import com.github.laxika.magicalvibes.cards.t.TitaniaGaeaIncarnate;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({SoulPartition.class, GrizzlyBears.class, Forest.class,
        ArgothSanctumOfNature.class, TitaniaVoiceOfGaea.class, TitaniaGaeaIncarnate.class})
class SoulPartitionTest extends BaseCardTest {

    private UUID exileOpponentsBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SoulPartition()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        return bears.getOriginalCard().getId();
    }

    @Test
    @DisplayName("Exiles a target nonland permanent and lets its owner play it from exile")
    void exilesTargetAndGrantsOwnerPlayPermission() {
        UUID bearsId = exileOpponentsBears();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(bearsId)).isNotNull();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castFromExile(player2, bearsId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A spell cast by the exiled card's owner as an opponent costs {2} more")
    void opponentPaysAdditionalTwoMana() {
        UUID bearsId = exileOpponentsBears();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player2, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The controller's own exiled card is not taxed")
    void controllerDoesNotPayTaxForOwnCard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID bearsId = bears.getOriginalCard().getId();

        harness.setHand(player1, List.of(new SoulPartition()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, bearsId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SoulPartition()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("Permission does not allow a creature to be cast during combat")
    void ownerMustRespectNormalCastingTiming() {
        UUID bearsId = exileOpponentsBears();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromExile(player2, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cast sorcery-speed spell from exile now");
        assertThat(gd.findExiledCard(bearsId)).isNotNull();
    }

    @Test
    @DisplayName("Only the owner receives permission to cast the exiled card")
    void casterCannotPlayOpponentsExiledCard() {
        UUID bearsId = exileOpponentsBears();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, bearsId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(bearsId)).isNotNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An unsuccessful cast retains permission for a later fully paid cast")
    void failedPaymentDoesNotConsumePermission() {
        UUID bearsId = exileOpponentsBears();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player2, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.findExiledCard(bearsId)).isNotNull();

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castFromExile(player2, bearsId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(bearsId)).isNull();
    }

    @Test
    @DisplayName("Exiling a melded permanent lets its owner play both component cards")
    void ownerCanPlayBothMeldComponents() {
        TitaniaVoiceOfGaea titania = new TitaniaVoiceOfGaea();
        ArgothSanctumOfNature argoth = new ArgothSanctumOfNature();
        harness.addToBattlefield(player2, titania);
        harness.addToBattlefield(player2, argoth);
        harness.setGraveyard(player2,
                List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        advanceToUpkeep(player2);
        resolveAllTriggers();

        Permanent melded = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof TitaniaGaeaIncarnate)
                .findFirst().orElseThrow();
        harness.setHand(player1, List.of(new SoulPartition()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, melded.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(titania.getId())).isNotNull();
        assertThat(gd.findExiledCard(argoth.getId())).isNotNull();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player2, argoth.getId());
        harness.assertOnBattlefield(player2, "Argoth, Sanctum of Nature");

        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castFromExile(player2, titania.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Titania, Voice of Gaea");
    }
}
