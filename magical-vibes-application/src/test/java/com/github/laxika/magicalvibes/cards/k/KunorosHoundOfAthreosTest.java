package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Ichthyomorphosis;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.cards.r.RiseFromTheGrave;
import com.github.laxika.magicalvibes.cards.r.RiseToGlory;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.cards.u.UnderworldCharger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KunorosHoundOfAthreos.class, GrizzlyBears.class, RiseFromTheGrave.class, ThinkTwice.class,
        Ichthyomorphosis.class, NyxbornCourser.class, RiseToGlory.class, UnderworldCharger.class})
class KunorosHoundOfAthreosTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents players from casting spells from graveyards")
    void preventsGraveyardSpellCasting() {
        harness.addToBattlefield(player1, new KunorosHoundOfAthreos());
        harness.setGraveyard(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFlashback(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevents creature cards in graveyards from entering the battlefield")
    void preventsCreatureReanimation() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new KunorosHoundOfAthreos());
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void preventsControllerFromCastingFlashback() {
        harness.addToBattlefield(player1, new KunorosHoundOfAthreos());
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Spells can't be cast from graveyards");
        harness.assertInGraveyard(player1, "Think Twice");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void preventsReanimationFromOpponentsGraveyard() {
        harness.addToBattlefield(player2, new KunorosHoundOfAthreos());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void preventsEscapeBeforePayingCosts() {
        harness.addToBattlefield(player1, new KunorosHoundOfAthreos());
        harness.setGraveyard(player1, List.of(new UnderworldCharger(),
                new NyxbornCourser(), new NyxbornCourser(), new NyxbornCourser()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Spells can't be cast from graveyards");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void permitsCreatureSpellsFromHand() {
        harness.addToBattlefield(player1, new KunorosHoundOfAthreos());
        harness.setHand(player1, List.of(new NyxbornCourser()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nyxborn Courser");
    }

    @Test
    void canReanimateKunorosWhenNoKunorosIsOnBattlefield() {
        KunorosHoundOfAthreos kunoros = new KunorosHoundOfAthreos();
        harness.setGraveyard(player1, List.of(kunoros));

        castRiseToGlory(0, kunoros.getId());

        harness.assertOnBattlefield(player1, "Kunoros, Hound of Athreos");
        harness.assertNotInGraveyard(player1, "Kunoros, Hound of Athreos");
    }

    @Test
    void losingAllAbilitiesAllowsCreatureReanimation() {
        var kunoros = harness.addToBattlefieldAndReturn(player2, new KunorosHoundOfAthreos());
        harness.setHand(player1, List.of(new Ichthyomorphosis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, kunoros.getId());
        harness.passBothPriorities();
        NyxbornCourser courser = new NyxbornCourser();
        harness.setGraveyard(player1, List.of(courser));

        castRiseToGlory(0, courser.getId());

        harness.assertOnBattlefield(player1, "Nyxborn Courser");
        harness.assertNotInGraveyard(player1, "Nyxborn Courser");
    }

    @Test
    void losingAllAbilitiesAllowsFlashback() {
        var kunoros = harness.addToBattlefieldAndReturn(player2, new KunorosHoundOfAthreos());
        harness.setHand(player1, List.of(new Ichthyomorphosis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, kunoros.getId());
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.setLibrary(player1, List.of(new NyxbornCourser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, null);

        harness.assertInHand(player1, "Nyxborn Courser");
        harness.assertNotInGraveyard(player1, "Think Twice");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Think Twice"));
    }

    @Test
    void permitsNoncreatureAuraToReturnFromGraveyard() {
        var kunoros = harness.addToBattlefieldAndReturn(player2, new KunorosHoundOfAthreos());
        Ichthyomorphosis aura = new Ichthyomorphosis();
        harness.setGraveyard(player1, List.of(aura));

        castRiseToGlory(1, aura.getId());
        harness.handlePermanentChosen(player1, kunoros.getId());

        harness.assertOnBattlefield(player1, "Ichthyomorphosis");
        harness.assertNotInGraveyard(player1, "Ichthyomorphosis");
    }

    @Test
    void preventsTargetedReturnOfEnchantmentCreature() {
        harness.addToBattlefield(player2, new KunorosHoundOfAthreos());
        NyxbornCourser courser = new NyxbornCourser();
        harness.setGraveyard(player1, List.of(courser));

        castRiseToGlory(0, courser.getId());

        harness.assertNotOnBattlefield(player1, "Nyxborn Courser");
        harness.assertInGraveyard(player1, "Nyxborn Courser");
    }

    private void castRiseToGlory(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new RiseToGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{mode},
                List.of(targetId), List.of());
        harness.passBothPriorities();
    }
}
