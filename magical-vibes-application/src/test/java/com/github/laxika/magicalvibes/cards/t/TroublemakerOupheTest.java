package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TroublemakerOuphe.class, DarksteelRelic.class, FountainOfYouth.class, AngelicChorus.class,
        GrizzlyBears.class})
class TroublemakerOupheTest extends BaseCardTest {

    @Test
    void withoutBargainDoesNotExileAnArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new TroublemakerOuphe()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals(target.getCard().getName()));
    }

    @Test
    void withBargainExilesAnArtifactAnOpponentControls() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new TroublemakerOuphe()));
        addMana();

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, target.getId(), null,
                List.of(), List.of(), false, sacrifice.getId(), null, null, null, null, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Fountain of Youth"));
        harness.assertInGraveyard(player1, "Darksteel Relic");
    }

    @Test
    void withBargainExilesAnEnchantmentAnOpponentControls() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new TroublemakerOuphe()));
        addMana();

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, target.getId(), null,
                List.of(), List.of(), false, sacrifice.getId(), null, null, null, null, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Angelic Chorus"));
    }

    @Test
    void bargainCannotTargetOwnArtifact() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new TroublemakerOuphe()));
        addMana();

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0,
                target.getId(), null, List.of(), List.of(), false, sacrifice.getId(), null, null, null, null, true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bargainCannotTargetCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TroublemakerOuphe()));
        addMana();

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0,
                target.getId(), null, List.of(), List.of(), false, sacrifice.getId(), null, null, null, null, true))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
