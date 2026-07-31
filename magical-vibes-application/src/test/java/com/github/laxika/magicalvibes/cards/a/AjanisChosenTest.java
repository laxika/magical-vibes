package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AjanisChosenTest extends BaseCardTest {

    private Permanent putAjanisChosen() {
        Permanent ajani = new Permanent(new AjanisChosen());
        gd.playerBattlefields.get(player1.getId()).add(ajani);
        return ajani;
    }

    private Permanent catToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.CAT))
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElse(null);
    }

    @Test
    @DisplayName("A non-Aura enchantment entering makes a 2/2 Cat token with no attach prompt")
    void nonAuraEnchantmentCreatesToken() {
        putAjanisChosen();
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve the enchantment (queues the trigger)
        harness.passBothPriorities(); // resolve the trigger

        Permanent token = catToken();
        assertThat(token).isNotNull();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3); // 2/2 + Glorious Anthem
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An Aura entering offers the move, and accepting attaches it to the new Cat token")
    void auraCanBeMovedToTheToken() {
        putAjanisChosen();
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities(); // resolve Pacifism onto Grizzly Bears
        Permanent pacifism = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isAura())
                .findFirst()
                .orElseThrow();
        assertThat(pacifism.getAttachedTo()).isEqualTo(bears.getId());

        harness.passBothPriorities(); // resolve the trigger
        harness.handleMayAbilityChosen(player1, true);

        Permanent token = catToken();
        assertThat(token).isNotNull();
        assertThat(pacifism.getAttachedTo()).isEqualTo(token.getId());
    }

    @Test
    @DisplayName("Declining the move leaves the Aura on its original host")
    void decliningLeavesAuraAttached() {
        putAjanisChosen();
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent pacifism = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isAura())
                .findFirst()
                .orElseThrow();
        assertThat(catToken()).isNotNull();
        assertThat(pacifism.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("An opponent's enchantment entering does not trigger")
    void opponentEnchantmentDoesNotTrigger() {
        putAjanisChosen();
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(catToken()).isNull();
    }
}
