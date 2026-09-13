package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({MartyrsBond.class, Disenchant.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class,
        LotusPetal.class, Shatter.class, StoneRain.class})
class MartyrsBondTest extends BaseCardTest {

    @Test
    @DisplayName("Makes each opponent sacrifice a permanent sharing a type with a dead artifact")
    void sacrificesMatchingArtifact() {
        harness.addToBattlefield(player1, new MartyrsBond());
        harness.addToBattlefield(player1, new LotusPetal());
        harness.addToBattlefield(player2, new LotusPetal());
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyPermanent(player1, "Lotus Petal", player2, new Shatter(), ManaColor.RED, 2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Lotus Petal");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger when another land you control dies")
    void doesNotTriggerForLand() {
        harness.addToBattlefield(player1, new MartyrsBond());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyPermanent(player1, "Forest", player2, new StoneRain(), ManaColor.RED, 3);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Triggers when Martyr's Bond itself is put into a graveyard")
    void sacrificesMatchingPermanentWhenItDies() {
        harness.addToBattlefield(player1, new MartyrsBond());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyPermanent(player1, "Martyr's Bond", player2, new Disenchant(), ManaColor.WHITE, 2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void destroyPermanent(Player player, String name, Player caster, Card spell,
            ManaColor manaColor, int manaAmount) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(spell));
        harness.addMana(caster, manaColor, manaAmount);
        UUID permanentId = harness.getPermanentId(player, name);
        if (spell instanceof Shatter || spell instanceof Disenchant) {
            harness.castInstant(caster, 0, permanentId);
        } else {
            harness.castSorcery(caster, 0, permanentId);
        }
        harness.passBothPriorities();
    }
}
