package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OsgoodOperationDouble.class, GrizzlyBears.class})
class OsgoodOperationDoubleTest extends BaseCardTest {

    @Test
    void castTriggerCreatesNonlegendaryTokenCopy() {
        harness.setHand(player1, List.of(new OsgoodOperationDouble()));
        addOsgoodMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        List<Permanent> copies = findPermanents(player1, "Osgood, Operation Double").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(copies).hasSize(1);
        assertThat(copies.getFirst().getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    void paradoxInvestigatesForSpellCastFromExileButNotFromHand() {
        harness.addToBattlefield(player1, new OsgoodOperationDouble());
        GrizzlyBears exiledSpell = new GrizzlyBears();
        gd.addToExile(player1.getId(), exiledSpell);
        gd.exilePlayPermissions.put(exiledSpell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromExile(player1, exiledSpell.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void activatedManaIsRestrictedToArtifacts() {
        Permanent osgood = addCreatureReady(player1, new OsgoodOperationDouble());
        harness.activateAbility(player1, 0, 0, null, null);

        Card artifact = new Card();
        artifact.setName("Test Artifact");
        artifact.setType(CardType.ARTIFACT);
        artifact.setManaCost("{1}");
        harness.setHand(player1, List.of(artifact));
        harness.castArtifact(player1, 0);

        assertThat(osgood.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> {
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.castCreature(player1, 0);
        }).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void addOsgoodMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
    }
}
