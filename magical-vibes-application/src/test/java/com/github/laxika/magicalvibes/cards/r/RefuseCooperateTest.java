package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RefuseCooperate.class, CounselOfTheSoratami.class, GrizzlyBears.class, Boomerang.class})
class RefuseCooperateTest extends BaseCardTest {

    @Test
    @DisplayName("Refuse deals damage equal to target spell mana value to that spell's controller")
    void refuseDamagesSpellControllerByManaValue() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami(); // MV 3
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new RefuseCooperate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, counsel.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        harness.assertInGraveyard(player2, "Refuse");
    }

    @Test
    @DisplayName("Refuse can target a creature spell")
    void refuseCanTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears(); // MV 2
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new RefuseCooperate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cooperate from graveyard copies target instant/sorcery then exiles")
    void cooperateCopiesSpellAndExiles() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setGraveyard(player2, List.of(new RefuseCooperate()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveFlashback(player2, 0, counsel.getId());

        GameData game = harness.getGameData();
        assertThat(game.stack).hasSize(2);
        StackEntry copy = game.stack.getLast();
        assertThat(copy.isCopy()).isTrue();
        assertThat(copy.getControllerId()).isEqualTo(player2.getId());
        assertThat(copy.getDescription()).isEqualTo("Copy of Counsel of the Soratami");

        assertThat(game.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Refuse") || c.getName().equals("Cooperate"));
        assertThat(game.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Refuse"));
    }

    @Test
    @DisplayName("Cooperate cannot target a creature spell")
    void cooperateCannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setGraveyard(player2, List.of(new RefuseCooperate()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castFlashback(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cooperate may be cast at instant speed")
    void cooperateCastsAtInstantSpeed() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setGraveyard(player2, List.of(new RefuseCooperate()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, bearsPermId);
        harness.passPriority(player1);
        harness.castAndResolveFlashback(player2, 0, boomerang.getId());

        assertThat(gd.stack).anySatisfy(se ->
                assertThat(se.getDescription()).isEqualTo("Copy of Boomerang"));
    }

    @Test
    @DisplayName("Cooperate copy of targeted spell offers retarget prompt")
    void cooperateOffersRetargetForTargetedSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setGraveyard(player2, List.of(new RefuseCooperate()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, bearsPermId);
        harness.passPriority(player1);
        harness.castAndResolveFlashback(player2, 0, boomerang.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Refuse uses Cooperate's mana value when targeting a copy of the aftermath half")
    void refuseUsesCopiedAftermathHalfManaValue() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.setGraveyard(player1, List.of(new RefuseCooperate()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.setGraveyard(player2, List.of(new RefuseCooperate()));
        harness.setHand(player2, List.of(new RefuseCooperate()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0);
        harness.castFlashback(player2, 0, counsel.getId());
        UUID cooperateId = gd.stack.getLast().getTargetableId();
        harness.castAndResolveFlashback(player1, 0, cooperateId);
        harness.handleMayAbilityChosen(player1, false);
        UUID copyId = gd.stack.getLast().getTargetableId();
        harness.castAndResolveInstant(player2, 0, copyId);

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Declining new targets keeps the original target for the copy")
    void cooperateCopyResolvesWithOriginalTargetWhenRetargetDeclined() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setGraveyard(player2, List.of(new RefuseCooperate()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, bearsId);
        harness.castAndResolveFlashback(player2, 0, boomerang.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(boomerang.getId());
    }

    @Test
    @DisplayName("Cooperate can change the copy's target without changing the original spell")
    void cooperateRetargetsOnlyTheCopy() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID originalTarget = harness.getPermanentId(player1, "Grizzly Bears");
        UUID newTarget = harness.getPermanentId(player2, "Grizzly Bears");
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setGraveyard(player2, List.of(new RefuseCooperate()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, originalTarget);
        harness.castAndResolveFlashback(player2, 0, boomerang.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, newTarget);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
