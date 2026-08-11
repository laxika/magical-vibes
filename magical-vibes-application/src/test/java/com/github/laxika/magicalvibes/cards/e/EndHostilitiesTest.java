package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.d.DaggerOfTheWorthy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EndHostilitiesTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and permanents attached to creatures")
    void destroysCreaturesAndTheirAttachments() {
        Permanent player1Creature = addCreatureReady(player1, new GrizzlyBears());
        attachPermanent(player1, new HolyStrength(), player1Creature);
        attachPermanent(player1, new DaggerOfTheWorthy(), player1Creature);

        Permanent player2Creature = addCreatureReady(player2, new GrizzlyBears());
        attachPermanent(player2, new HolyStrength(), player2Creature);

        harness.addToBattlefield(player1, new RuleOfLaw());
        Permanent unattachedDagger = harness.addToBattlefieldAndReturn(player1, new DaggerOfTheWorthy());
        harness.addToBattlefield(player2, new AngelicChorus());

        castEndHostilities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.assertNotOnBattlefield(player2, "Holy Strength");
        harness.assertOnBattlefield(player1, "Rule of Law");
        harness.assertOnBattlefield(player2, "Angelic Chorus");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(findPermanent(player1, "Rule of Law"), unattachedDagger);
    }

    private void castEndHostilities() {
        harness.setHand(player1, List.of(new EndHostilities()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private Permanent attachPermanent(Player owner, Card card, Permanent creature) {
        Permanent attachment = new Permanent(card);
        attachment.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(owner.getId()).add(attachment);
        return attachment;
    }
}
