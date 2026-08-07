package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PiaAndKiranNalaarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates two 1/1 colorless Thopter artifact creature tokens with flying")
    void etbCreatesTwoThopters() {
        harness.setHand(player1, List.of(new PiaAndKiranNalaar()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> thopters = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(thopters).hasSize(2);
        assertThat(thopters).allSatisfy(thopter -> {
            assertThat(thopter.getCard().getName()).isEqualTo("Thopter");
            assertThat(thopter.getCard().getPower()).isEqualTo(1);
            assertThat(thopter.getCard().getToughness()).isEqualTo(1);
            assertThat(thopter.getCard().getColors()).isEmpty();
            assertThat(thopter.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
        });
    }

    @Test
    @DisplayName("Ability sacrifices an artifact and deals 2 damage to target player")
    void dealsDamageToPlayer() {
        harness.addToBattlefield(player1, new PiaAndKiranNalaar());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertNotOnBattlefield(player1, "Spellbook");

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ability deals 2 damage to target creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player1, new PiaAndKiranNalaar());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID elvesId = findPermanent(player2, "Llanowar Elves").getId();

        harness.activateAbility(player1, 0, null, elvesId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Ability can be activated the turn it enters — it does not require tapping")
    void doesNotRequireTap() {
        harness.addToBattlefield(player1, new PiaAndKiranNalaar());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(findPermanent(player1, "Pia and Kiran Nalaar").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        harness.addToBattlefield(player1, new PiaAndKiranNalaar());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }
}
