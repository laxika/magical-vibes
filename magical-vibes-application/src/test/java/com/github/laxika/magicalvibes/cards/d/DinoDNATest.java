package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DinoDNA.class, GrizzlyBears.class})
class DinoDNATest extends BaseCardTest {

    @Test
    void imprintsTargetCreatureFromAnyGraveyard() {
        DinoDNA dna = new DinoDNA();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, dna);
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(permanent.getId())).containsExactly(creature);
        assertThat(gd.getImprintedCard(dna)).isSameAs(creature);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void createsTargetedGreenDinosaurTokenCopy() {
        DinoDNA dna = new DinoDNA();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, dna);
        GrizzlyBears creature = new GrizzlyBears();
        gd.addToExile(player1.getId(), creature, permanent.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, creature.getId(), Zone.EXILE);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DINOSAUR);
        assertThat(token.getCard().getPower()).isEqualTo(6);
        assertThat(token.getCard().getToughness()).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void cannotTargetCreatureNotExiledWithThisArtifact() {
        harness.addToBattlefieldAndReturn(player1, new DinoDNA());
        GrizzlyBears creature = new GrizzlyBears();
        gd.addToExile(player1.getId(), creature);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, creature.getId(), Zone.EXILE))
                .isInstanceOf(IllegalStateException.class);
    }
}
