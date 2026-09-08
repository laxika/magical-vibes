package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SekKuarDeathkeeperTest extends BaseCardTest {

    @Test
    void createsGravebornWhenAnotherNontokenCreatureYouControlDies() {
        harness.addToBattlefield(player1, new SekKuarDeathkeeper());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player2, player1, "Grizzly Bears");
        harness.passBothPriorities();

        Permanent graveborn = findPermanent(player1, "Graveborn");
        assertThat(graveborn.getCard().getPower()).isEqualTo(3);
        assertThat(graveborn.getCard().getToughness()).isEqualTo(1);
        assertThat(graveborn.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(graveborn.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
        assertThat(graveborn.getCard().getSubtypes()).contains(CardSubtype.GRAVEBORN);
        assertThat(graveborn.getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    void doesNotTriggerWhenSekKuarDies() {
        harness.addToBattlefield(player1, new SekKuarDeathkeeper());

        killWithShock(player2, player1, "Sek'Kuar, Deathkeeper");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Graveborn")).isEmpty();
    }

    @Test
    void doesNotTriggerWhenTokenCreatureYouControlDies() {
        harness.addToBattlefield(player1, new SekKuarDeathkeeper());
        harness.addToBattlefield(player1, new GravebornToken());

        killWithShock(player2, player1, "Graveborn Token");

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Graveborn")).isEmpty();
    }

    @Test
    void doesNotTriggerForAnOpponentsCreature() {
        harness.addToBattlefield(player1, new SekKuarDeathkeeper());
        harness.addToBattlefield(player2, new GrizzlyBears());

        killWithShock(player1, player2, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Graveborn")).isEmpty();
    }

    private void killWithShock(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }

    private static class GravebornToken extends Card {

        private GravebornToken() {
            setName("Graveborn Token");
            setType(CardType.CREATURE);
            setPower(1);
            setToughness(1);
            setToken(true);
        }
    }
}
