package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lifeforce;
import com.github.laxika.magicalvibes.cards.n.NeutralizingBlast;
import com.github.laxika.magicalvibes.cards.s.SternScolding;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WickerwingEffigy.class, GrizzlyBears.class, Lifeforce.class, SternScolding.class, TrainedArmodon.class,
        NeutralizingBlast.class})
class WickerwingEffigyTest extends BaseCardTest {

    @Test
    @DisplayName("casts a creature from the top of the library and changes its characteristics")
    void changesCreatureCastFromLibrary() {
        harness.addToBattlefield(player1, new WickerwingEffigy());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFromLibraryTop(player1);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveColors(gd, entered)).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, entered)).contains(CardSubtype.BIRD);
        assertThat(gqs.hasKeyword(gd, entered, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, entered)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, entered)).isEqualTo(1);
    }

    @Test
    @DisplayName("does not change a creature cast from hand")
    void doesNotChangeCreatureCastFromHand() {
        harness.addToBattlefield(player1, new WickerwingEffigy());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveColors(gd, entered)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, entered)).doesNotContain(CardSubtype.BIRD);
        assertThat(gqs.hasKeyword(gd, entered, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, entered)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, entered)).isEqualTo(2);
    }

    @Test
    @DisplayName("The changed spell can be countered as a black spell")
    void modifiedSpellCanBeCounteredAsABlackSpell() {
        harness.addToBattlefield(player1, new WickerwingEffigy());
        harness.addToBattlefield(player2, new Lifeforce());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castAndResolveFromLibraryTop(player1);
        harness.activateAbility(player2, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The changed spell has base power and toughness 1/1 on the stack, so Stern Scolding can target it")
    void changedSpellHasBasePowerAndToughnessOnTheStack() {
        harness.addToBattlefield(player1, new WickerwingEffigy());
        TrainedArmodon armodon = new TrainedArmodon();
        harness.setLibrary(player1, List.of(armodon));
        harness.setHand(player2, List.of(new SternScolding()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveFromLibraryTop(player1);
        harness.castInstant(player2, 0, armodon.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Trained Armodon");
        harness.assertNotOnBattlefield(player1, "Trained Armodon");
    }

    @Test
    @DisplayName("The changed spell is green and black on the stack, so Neutralizing Blast can counter it as a multicolored spell")
    void changedSpellIsMulticoloredOnTheStack() {
        harness.addToBattlefield(player1, new WickerwingEffigy());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.setHand(player2, List.of(new NeutralizingBlast()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveFromLibraryTop(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The top noncreature card is visible only to its controller and cannot be cast")
    void topNoncreatureIsVisibleOnlyToControllerAndCannotBeCast() {
        harness.addToBattlefield(player1, new WickerwingEffigy());
        Lifeforce top = new Lifeforce();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{") && message.contains(top.getId().toString()));
        assertThat(harness.getConn2().getSentMessages()).noneMatch(message ->
                message.contains(top.getId().toString()));
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("The library permission does not grant flash")
    void libraryPermissionDoesNotGrantFlash() {
        harness.addToBattlefield(player1, new WickerwingEffigy());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("The library permission still requires paying the spell's cost")
    void libraryPermissionStillRequiresPayment() {
        harness.addToBattlefield(player1, new WickerwingEffigy());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
}
