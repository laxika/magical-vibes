package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KingpinsEnforcers.class, GrizzlyBears.class, Spellbook.class})
class KingpinsEnforcersTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature draws a card")
    void sacrificesCreatureAndDrawsCard() {
        Permanent enforcers = harness.addToBattlefieldAndReturn(player1, new KingpinsEnforcers());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(enforcers).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Sacrificing an artifact draws a card")
    void sacrificesArtifactAndDrawsCard() {
        Permanent enforcers = harness.addToBattlefieldAndReturn(player1, new KingpinsEnforcers());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(enforcers).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("The source creature may be sacrificed to draw a card")
    void sacrificesItselfAndDrawsCard() {
        Permanent enforcers = harness.addToBattlefieldAndReturn(player1, new KingpinsEnforcers());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(enforcers);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enforcers.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
