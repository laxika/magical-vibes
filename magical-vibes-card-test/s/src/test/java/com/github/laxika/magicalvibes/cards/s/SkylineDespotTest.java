package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.q.QueenMarchesa;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;



@CardUsed({SkylineDespot.class, QueenMarchesa.class})
class SkylineDespotTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes the monarch when it enters")
    void becomesMonarchOnEntry() {
        harness.enterBattlefieldAndReturn(player1, new SkylineDespot());
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Creates a 5/5 flying Dragon during the controller's upkeep while monarch")
    void createsDragonWhileControllerIsMonarch() {
        harness.addToBattlefield(player1, new SkylineDespot());
        gd.monarchPlayerId = player1.getId();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        Permanent dragon = findPermanent(player1, "Dragon");
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(5);
        assertThat(dragon.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(dragon.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not create a Dragon when its controller is not monarch")
    void doesNotCreateDragonWhileControllerIsNotMonarch() {
        harness.addToBattlefield(player1, new SkylineDespot());
        gd.monarchPlayerId = player2.getId();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Dragon")).isZero();
    }
    @Test
    void entersAndMakesItsControllerTheMonarch() {
        harness.enterBattlefieldAndReturn(player1, new SkylineDespot());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void createsAFlyingDragonDuringUpkeepWhileItsControllerIsTheMonarch() {
        harness.enterBattlefieldAndReturn(player1, new SkylineDespot());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Dragon");
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(5);
        assertThat(dragon.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(dragon.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
    }

    @Test
    void doesNotCreateADragonDuringUpkeepWhenAnOpponentIsTheMonarch() {
        harness.enterBattlefieldAndReturn(player1, new SkylineDespot());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player2, new QueenMarchesa());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dragon")).isEmpty();
    }

    @Test
    void doesNotTriggerWhenThereIsNoMonarch() {
        harness.addToBattlefield(player1, new SkylineDespot());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Dragon")).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.enterBattlefieldAndReturn(player1, new SkylineDespot());
        resolveAllTriggers();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Dragon")).isEmpty();
    }

    @Test
    void doesNotCreateDragonIfMonarchyIsLostBeforeResolution() {
        harness.enterBattlefieldAndReturn(player1, new SkylineDespot());
        resolveAllTriggers();
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.enterBattlefieldAndReturn(player2, new QueenMarchesa());
        harness.passBothPriorities();
        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dragon")).isEmpty();
    }

    @Test
    void becomingMonarchAfterUpkeepBeginsDoesNotCreateDragon() {
        harness.addToBattlefield(player1, new SkylineDespot());
        harness.enterBattlefieldAndReturn(player2, new QueenMarchesa());
        resolveAllTriggers();

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        harness.enterBattlefieldAndReturn(player1, new QueenMarchesa());
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        assertThat(findPermanents(player1, "Dragon")).isEmpty();
    }

}
