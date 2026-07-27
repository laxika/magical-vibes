package com.github.laxika.magicalvibes.service.aura;

import com.github.laxika.magicalvibes.cards.a.ApostlesBlessing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpiritLink;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

/**
 * End-to-end CR 704.5n check: an aura whose enchanted creature gains protection from the
 * aura's color is put into its owner's graveyard by state-based actions.
 */
class AttachmentLegalityIntegrationTest extends BaseCardTest {

    @Test
    @DisplayName("White aura falls off when the enchanted creature gains protection from white")
    void auraFallsOffWhenCreatureGainsProtectionFromItsColor() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        Permanent spiritLink = new Permanent(new SpiritLink());
        spiritLink.setAttachedTo(bearsId);
        gd.playerBattlefields.get(player1.getId()).add(spiritLink);

        harness.setHand(player1, List.of(new ApostlesBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        harness.assertNotOnBattlefield(player1, "Spirit Link");
        harness.assertInGraveyard(player1, "Spirit Link");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Aura stays attached when the chosen protection color does not match it")
    void auraStaysWhenProtectionColorDoesNotMatch() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        Permanent spiritLink = new Permanent(new SpiritLink());
        spiritLink.setAttachedTo(bearsId);
        gd.playerBattlefields.get(player1.getId()).add(spiritLink);

        harness.setHand(player1, List.of(new ApostlesBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.assertOnBattlefield(player1, "Spirit Link");
        harness.assertNotInGraveyard(player1, "Spirit Link");
    }
}
