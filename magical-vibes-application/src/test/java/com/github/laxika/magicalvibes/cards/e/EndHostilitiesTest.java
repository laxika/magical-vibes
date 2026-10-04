package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.d.DaggerOfTheWorthy;
import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.g.GhostfireBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.k.KinTreeWarden;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EndHostilities.class, AngelicChorus.class, DaggerOfTheWorthy.class,
        GrizzlyBears.class, HolyStrength.class, RuleOfLaw.class, DarksteelMyr.class,
        GhostfireBlade.class, KinTreeWarden.class})
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

    @Test
    @DisplayName("Destroys attachments even when their creature is indestructible")
    void destroysAttachmentsOnIndestructibleCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());
        attachPermanent(player1, new GhostfireBlade(), creature);
        attachPermanent(player2, new HolyStrength(), creature);

        castEndHostilities();

        harness.assertOnBattlefield(player2, "Darksteel Myr");
        harness.assertNotInGraveyard(player2, "Darksteel Myr");
        harness.assertInGraveyard(player1, "Ghostfire Blade");
        harness.assertInGraveyard(player2, "Holy Strength");
    }

    @Test
    @DisplayName("Allows regeneration but destroys Equipment attached to the regenerated creature")
    void regenerationDoesNotSaveEquipment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KinTreeWarden());
        attachPermanent(player1, new GhostfireBlade(), creature);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        castEndHostilities();

        harness.assertOnBattlefield(player1, "Kin-Tree Warden");
        harness.assertNotInGraveyard(player1, "Kin-Tree Warden");
        assertThat(creature.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Ghostfire Blade");
    }

    @Test
    @DisplayName("Leaves unattached Equipment alone when there are no creatures")
    void resolvesWithoutCreatures() {
        harness.addToBattlefield(player1, new GhostfireBlade());
        harness.addToBattlefield(player2, new GhostfireBlade());

        castEndHostilities();

        harness.assertOnBattlefield(player1, "Ghostfire Blade");
        harness.assertOnBattlefield(player2, "Ghostfire Blade");
        harness.assertInGraveyard(player1, "End Hostilities");
    }

    private void castEndHostilities() {
        harness.setHand(player1, List.of(new EndHostilities()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private Permanent attachPermanent(Player owner, Card card, Permanent creature) {
        Permanent attachment = harness.addToBattlefieldAndReturn(owner, card);
        attachment.setAttachedTo(creature.getId());
        return attachment;
    }
}
