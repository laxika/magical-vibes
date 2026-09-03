package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CircleOfSolace.class, ProdigalSorcerer.class})
class CircleOfSolaceTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type as Circle of Solace enters controls its prevention ability")
    void choosesCreatureTypeAsItEnters() {
        Permanent wizard = addCreatureReady(player2, new ProdigalSorcerer());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new CircleOfSolace()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WIZARD");

        Permanent circle = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof CircleOfSolace)
                .findFirst()
                .orElseThrow();
        assertThat(circle.getChosenSubtype()).isEqualTo(CardSubtype.WIZARD);

        activatePrevention(circle);
        activateDamage(wizard);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Only the next damage from a matching creature is prevented")
    void onlyNextMatchingDamageIsPrevented() {
        harness.setLife(player1, 20);
        Permanent circle = addCircle(player1, CardSubtype.WIZARD);
        Permanent firstWizard = addCreatureReady(player2, new ProdigalSorcerer());
        Permanent secondWizard = addCreatureReady(player2, new ProdigalSorcerer());

        activatePrevention(circle);
        activateDamage(firstWizard);
        activateDamage(secondWizard);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerNextDamageFromMatchingSourcesPrevented).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Damage from a different creature type is not prevented")
    void differentCreatureTypeDealsDamageNormally() {
        harness.setLife(player1, 20);
        Permanent circle = addCircle(player1, CardSubtype.GIANT);
        Permanent nonWizard = addCreatureReady(player2, new ProdigalSorcerer());

        activatePrevention(circle);
        activateDamage(nonWizard);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerNextDamageFromMatchingSourcesPrevented).containsKey(player1.getId());
    }

    private Permanent addCircle(Player player, CardSubtype chosenSubtype) {
        Permanent circle = new Permanent(new CircleOfSolace());
        circle.setChosenSubtype(chosenSubtype);
        gd.playerBattlefields.get(player.getId()).add(circle);
        return circle;
    }

    private void activatePrevention(Permanent circle) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(circle), null, null);
        harness.passBothPriorities();
    }

    private void activateDamage(Permanent wizard) {
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(wizard), null,
                player1.getId());
        harness.passBothPriorities();
    }
}
