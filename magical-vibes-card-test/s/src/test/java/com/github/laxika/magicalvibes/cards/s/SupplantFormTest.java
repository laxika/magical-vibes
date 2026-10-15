package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.ArashinCleric;
import com.github.laxika.magicalvibes.cards.a.AbzanSkycaptain;
import com.github.laxika.magicalvibes.cards.o.OjerTaqDeepestFoundation;
import com.github.laxika.magicalvibes.cards.t.TempleOfCivilization;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SupplantForm.class, GrizzlyBears.class, Forest.class, ArashinCleric.class, AbzanSkycaptain.class,
        OjerTaqDeepestFoundation.class, TempleOfCivilization.class})
class SupplantFormTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature to its owner's hand and creates a token copy")
    void returnsCreatureAndCreatesTokenCopy() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SupplantForm()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new SupplantForm()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        var target = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canReturnOwnCreatureAndCopiedEnterAbilityTriggers() {
        var target = harness.addToBattlefieldAndReturn(player1, new ArashinCleric());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SupplantForm()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Arashin Cleric");
        assertThat(findPermanents(player1, "Arashin Cleric")).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
        harness.assertLife(player1, 23);
    }

    @Test
    void illegalTargetAtResolutionCreatesNoToken() {
        var target = harness.addToBattlefieldAndReturn(player2, new AbzanSkycaptain());
        harness.setHand(player1, List.of(new SupplantForm()));
        harness.setHand(player2, List.of(new SupplantForm()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player2, "Abzan Skycaptain")).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
        harness.assertInHand(player2, "Abzan Skycaptain");
        harness.assertInGraveyard(player1, "Supplant Form");
    }

    @Test
    void copyOfFaceDownCreatureDoesNotCopyPrintedEnterAbility() {
        var target = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        target.setManifested(true);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SupplantForm()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Arashin Cleric");
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard().isToken()).isTrue();
                    assertThat(permanent.isFaceDown()).isFalse();
                });
        harness.assertLife(player1, 20);
    }

    @Test
    void creatureTokenCopyIsTripledByOjerTaq() {
        harness.addToBattlefield(player1, new OjerTaqDeepestFoundation());
        var target = harness.addToBattlefieldAndReturn(player2, new AbzanSkycaptain());
        harness.setHand(player1, List.of(new SupplantForm()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInHand(player2, "Abzan Skycaptain");
        assertThat(findPermanents(player1, "Abzan Skycaptain")).hasSize(3)
                .allSatisfy(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
    }
}
