package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Ixidron.class, GrizzlyBears.class})
class IxidronTest extends BaseCardTest {

    @Test
    void turnsOtherNontokenCreaturesFaceDownAndCountsThem() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        castIxidron();

        Permanent ixidron = findPermanent(player1, "Ixidron");
        assertThat(ownCreature.isFaceDown()).isTrue();
        assertThat(opposingCreature.isFaceDown()).isTrue();
        assertThat(ixidron.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, ixidron)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ixidron)).isEqualTo(2);
    }

    @Test
    void excludesCreatureTokensFromTurningFaceDownAndFromItsCount() {
        Permanent nontokenCreature = addCreatureReady(player1, new GrizzlyBears());
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent tokenCreature = new Permanent(tokenCard);
        tokenCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(tokenCreature);

        castIxidron();

        Permanent ixidron = findPermanent(player1, "Ixidron");
        assertThat(nontokenCreature.isFaceDown()).isTrue();
        assertThat(tokenCreature.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, ixidron)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ixidron)).isEqualTo(1);
    }

    @Test
    void updatesItsPowerAndToughnessWhenFaceDownCreaturesLeave() {
        Permanent removedCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        castIxidron();

        Permanent ixidron = findPermanent(player1, "Ixidron");
        assertThat(gqs.getEffectivePower(gd, ixidron)).isEqualTo(2);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, removedCreature));

        assertThat(gqs.getEffectivePower(gd, ixidron)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ixidron)).isEqualTo(1);
    }

    private void castIxidron() {
        harness.setHand(player1, List.of(new Ixidron()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
