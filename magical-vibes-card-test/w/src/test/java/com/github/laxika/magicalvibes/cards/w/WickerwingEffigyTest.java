package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lifeforce;
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

@CardUsed({WickerwingEffigy.class, GrizzlyBears.class, Lifeforce.class})
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
