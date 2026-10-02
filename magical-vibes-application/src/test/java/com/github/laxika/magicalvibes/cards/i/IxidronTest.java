package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AcademyRuins;
import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Ixidron.class, AshcoatBear.class, AcademyRuins.class})
class IxidronTest extends BaseCardTest {

    @Test
    void turnsOtherNontokenCreaturesFaceDownAndCountsThem() {
        Permanent ownCreature = addCreatureReady(player1, new AshcoatBear());
        Permanent opposingCreature = addCreatureReady(player2, new AshcoatBear());

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
        Permanent nontokenCreature = addCreatureReady(player1, new AshcoatBear());
        Card tokenCard = new AshcoatBear();
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
    void countsExistingFaceDownCreaturesLeavesNoncreaturesFaceUpAndUsesTwoTwoCharacteristics() {
        Permanent existingFaceDownCreature = addCreatureReady(player1, new AshcoatBear());
        existingFaceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent creature = addCreatureReady(player2, new AshcoatBear());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new AcademyRuins());

        castIxidron();

        Permanent ixidron = findPermanent(player1, "Ixidron");
        assertThat(existingFaceDownCreature.isFaceDown()).isTrue();
        assertThat(creature.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(noncreature.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, ixidron)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ixidron)).isEqualTo(2);
    }

    @Test
    void updatesItsPowerAndToughnessWhenFaceDownCreaturesLeave() {
        Permanent removedCreature = addCreatureReady(player1, new AshcoatBear());
        addCreatureReady(player2, new AshcoatBear());

        castIxidron();

        Permanent ixidron = findPermanent(player1, "Ixidron");
        assertThat(gqs.getEffectivePower(gd, ixidron)).isEqualTo(2);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, removedCreature));

        assertThat(gqs.getEffectivePower(gd, ixidron)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ixidron)).isEqualTo(1);
    }

    private void castIxidron() {
        harness.castFromHand(player1, new Ixidron(), "{3}{U}{U}");
        harness.passBothPriorities();
    }
}
