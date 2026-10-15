package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MalleableImpostor.class, GrizzlyBears.class})
class MalleableImpostorTest extends BaseCardTest {

    @Test
    void copiesAnOpponentsCreatureWithItsCopyExceptions() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MalleableImpostor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, targetId);

        Permanent impostor = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Malleable Impostor"))
                .findFirst()
                .orElse(null);

        assertThat(impostor).isNotNull();
        assertThat(impostor.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(impostor.getCard().getPower()).isEqualTo(2);
        assertThat(impostor.getCard().getToughness()).isEqualTo(2);
        assertThat(impostor.getCard().getSubtypes())
                .contains(CardSubtype.BEAR, CardSubtype.FAERIE, CardSubtype.SHAPESHIFTER);
        assertThat(impostor.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    void cannotCopyACreatureControlledByItsController() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MalleableImpostor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Malleable Impostor");
        harness.assertInGraveyard(player1, "Malleable Impostor");
    }
}
