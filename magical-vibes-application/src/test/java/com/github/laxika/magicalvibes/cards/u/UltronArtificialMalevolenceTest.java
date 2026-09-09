package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UltronArtificialMalevolence.class, AccordersShield.class, GoldMyr.class})
class UltronArtificialMalevolenceTest extends BaseCardTest {

    @Test
    void noncreatureArtifactCopyBecomesRobotVillainCreature() {
        addUltronReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new AccordersShield()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent token = findToken(player1, "Accorder's Shield");
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.ROBOT, CardSubtype.VILLAIN);
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void creatureArtifactCopyKeepsItsCopiedCharacteristics() {
        addUltronReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of(new GoldMyr()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent token = findToken(player1, "Gold Myr");
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getSubtypes())
                .doesNotContain(CardSubtype.ROBOT, CardSubtype.VILLAIN);
    }

    private Permanent addUltronReady(Player player) {
        return addCreatureReady(player, new UltronArtificialMalevolence());
    }

    private Permanent findToken(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
