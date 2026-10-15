package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.VampireOfTheDireMoon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScionOfOpulence.class, VampireOfTheDireMoon.class, Shock.class,
        DarksteelRelic.class, Forest.class})
class ScionOfOpulenceTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure when it dies")
    void createsTreasureWhenItDies() {
        Permanent scion = harness.addToBattlefieldAndReturn(player1, new ScionOfOpulence());

        destroyWithShock(scion);

        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    @Test
    @DisplayName("Creates a Treasure when another nontoken Vampire you control dies")
    void createsTreasureWhenAnotherVampireDies() {
        harness.addToBattlefield(player1, new ScionOfOpulence());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireOfTheDireMoon());

        destroyWithShock(vampire);

        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    @Test
    @DisplayName("Sacrifices two artifacts to exile the top card with play permission")
    void sacrificesTwoArtifactsToExileTopCard() {
        harness.addToBattlefield(player1, new ScionOfOpulence());
        harness.addToBattlefield(player1, new DarksteelRelic());
        harness.addToBattlefield(player1, new DarksteelRelic());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    private void destroyWithShock(Permanent target) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
