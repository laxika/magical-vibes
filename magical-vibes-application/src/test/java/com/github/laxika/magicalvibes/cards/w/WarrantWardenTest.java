package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WarrantWardenTest extends BaseCardTest {

    private static final int WARRANT = 0;
    private static final int WARDEN = 1;

    @Test
    @DisplayName("Warrant puts an attacking creature on top of its owner's library")
    void warrantPutsAttackingCreatureOnTopOfLibrary() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setLibrary(player2, List.of(new Plains()));

        harness.setHand(player1, List.of(new WarrantWarden()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castModalInstant(player1, 0, WARRANT, List.of(attacker.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()).get(0)).isSameAs(attacker.getCard());
    }

    @Test
    @DisplayName("Warrant can target a blocking creature")
    void warrantPutsBlockingCreatureOnTopOfLibrary() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        harness.setLibrary(player2, List.of(new Plains()));

        harness.setHand(player1, List.of(new WarrantWarden()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castModalInstant(player1, 0, WARRANT, List.of(blocker.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()).get(0)).isSameAs(blocker.getCard());
    }

    @Test
    @DisplayName("Warrant cannot target a creature that is not attacking or blocking")
    void warrantCannotTargetNonCombatCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WarrantWarden()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, WARRANT, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    @DisplayName("Warden creates a 4/4 white and blue Sphinx with flying and vigilance")
    void wardenCreatesSphinxToken() {
        harness.setHand(player1, List.of(new WarrantWarden()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, WARDEN, List.of());
        harness.passBothPriorities();

        Permanent sphinx = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(sphinx.getCard().getName()).isEqualTo("Sphinx");
        assertThat(sphinx.getCard().getPower()).isEqualTo(4);
        assertThat(sphinx.getCard().getToughness()).isEqualTo(4);
        assertThat(sphinx.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(sphinx.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(sphinx.getCard().getSubtypes()).containsExactly(CardSubtype.SPHINX);
        assertThat(sphinx.getCard().getKeywords()).containsExactlyInAnyOrder(Keyword.FLYING, Keyword.VIGILANCE);
    }
}
