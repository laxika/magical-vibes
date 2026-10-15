package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.j.JaceMirrorMage;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LaunchMishap.class, GrizzlyBears.class, JaceMirrorMage.class, Hurricane.class})
class LaunchMishapTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell and creates a Thopter")
    void countersCreatureSpellAndCreatesThopter() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new LaunchMishap()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));
        assertThat(thopters(player2)).hasSize(1).allSatisfy(thopter -> {
            assertThat(thopter.getCard().getPower()).isEqualTo(1);
            assertThat(thopter.getCard().getToughness()).isEqualTo(1);
            assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(thopter.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
            assertThat(thopter.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(thopter.getCard().getColor()).isNull();
        });
    }

    @Test
    @DisplayName("Counters a planeswalker spell and creates a Thopter")
    void countersPlaneswalkerSpellAndCreatesThopter() {
        JaceMirrorMage jace = new JaceMirrorMage();
        harness.setHand(player1, List.of(jace));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castPlaneswalker(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new LaunchMishap()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player2, 0, jace.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(jace.getId()));
        assertThat(thopters(player2)).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonplaneswalker spell")
    void cannotTargetOtherSpell() {
        Hurricane hurricane = new Hurricane();
        harness.setHand(player1, List.of(hurricane));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 1);
        harness.passPriority(player1);

        LaunchMishap launchMishap = new LaunchMishap();
        harness.setHand(player2, List.of(launchMishap));
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, hurricane.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Permanent> thopters(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
