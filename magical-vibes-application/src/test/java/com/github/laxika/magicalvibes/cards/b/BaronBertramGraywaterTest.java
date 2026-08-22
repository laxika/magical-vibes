package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PristineTalisman;
import com.github.laxika.magicalvibes.cards.q.QueensCommission;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaronBertramGraywater.class, QueensCommission.class, GrizzlyBears.class, PristineTalisman.class})
class BaronBertramGraywaterTest extends BaseCardTest {

    @Test
    void createsOneLifelinkVampireRogueWhenTokensEnter() {
        addCreatureReady(player1, new BaronBertramGraywater());
        harness.setHand(player1, List.of(new QueensCommission()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Vampire Rogue");
        assertThat(token.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(countPermanents(player1, "Vampire Rogue")).isEqualTo(1);
    }

    @Test
    void tokenTriggerFiresOnlyOnceEachTurn() {
        addCreatureReady(player1, new BaronBertramGraywater());
        harness.setHand(player1, List.of(new QueensCommission(), new QueensCommission()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Vampire Rogue")).isEqualTo(1);
    }

    @Test
    void sacrificesAnotherCreatureAndDrawsACard() {
        addCreatureReady(player1, new BaronBertramGraywater());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void sacrificesAnArtifactAndDrawsACard() {
        addCreatureReady(player1, new BaronBertramGraywater());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PristineTalisman());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Pristine Talisman");
    }
}
