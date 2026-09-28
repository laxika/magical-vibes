package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PersistentPetitioners;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheMasterTranscendent.class, GrizzlyBears.class, PersistentPetitioners.class, SerraAngel.class})
class TheMasterTranscendentTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives two rad counters to the chosen player")
    void entersGivesTwoRadCountersToTargetPlayer() {
        harness.enterBattlefieldAndReturn(player1, new TheMasterTranscendent());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerRadCounters.get(player1.getId())).isNull();
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Tap ability returns a milled creature as a green Mutant 3/3")
    void returnsMilledCreatureAsGreenMutant() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new TheMasterTranscendent());
        master.setSummoningSick(false);

        for (int i = 0; i < 4; i++) {
            Permanent petitioners = harness.addToBattlefieldAndReturn(player2, new PersistentPetitioners());
            petitioners.setSummoningSick(false);
        }
        Card milledCreature = new SerraAngel();
        harness.setLibrary(player2, List.of(milledCreature));
        harness.activateAbility(player2, 3, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, milledCreature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(milledCreature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectiveColors(gd, returned)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.MUTANT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.ANGEL)).isFalse();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(milledCreature);
    }

    @Test
    @DisplayName("Tap ability cannot target a creature that was not milled this turn")
    void cannotTargetCreatureNotMilledThisTurn() {
        harness.addToBattlefield(player1, new TheMasterTranscendent());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
