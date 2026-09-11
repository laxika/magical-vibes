package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesolationOfSmaug.class, GrizzlyBears.class, ShivanDragon.class})
class DesolationOfSmaugTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to each non-Dragon creature")
    void damagesNonDragonsOnly() {
        Permanent ownNonDragon = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownDragon = harness.addToBattlefieldAndReturn(player1, new ShivanDragon());
        Permanent opponentNonDragon = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentDragon = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());

        castDesolationOfSmaug();
        chooseFourManaColors(ManaColor.RED.name());

        assertThat(ownNonDragon.getMarkedDamage()).isEqualTo(3);
        assertThat(opponentNonDragon.getMarkedDamage()).isEqualTo(3);
        assertThat(ownDragon.getMarkedDamage()).isZero();
        assertThat(opponentDragon.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownDragon).doesNotContain(ownNonDragon);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentDragon).doesNotContain(opponentNonDragon);
    }

    @Test
    @DisplayName("Adds four mana in separately chosen colors for Dragon spells")
    void addsDragonSpellOnlyManaInAnyCombinationOfColors() {
        castDesolationOfSmaug();
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.DRAGON))).isEqualTo(4);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new ShivanDragon()));
        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.DRAGON))).isZero();
    }

    @Test
    @DisplayName("Dragon spell-only mana cannot cast a non-Dragon spell")
    void dragonSpellOnlyManaCannotCastNonDragonSpell() {
        castDesolationOfSmaug();
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDesolationOfSmaug() {
        harness.setHand(player1, List.of(new DesolationOfSmaug()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void chooseFourManaColors(String color) {
        harness.handleListChoice(player1, color);
        harness.handleListChoice(player1, color);
        harness.handleListChoice(player1, color);
        harness.handleListChoice(player1, color);
    }
}
