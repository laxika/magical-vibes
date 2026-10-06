package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.i.InvisibleStalker;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.t.TyphoidRats;
import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeverTheBloodline.class, WalkingCorpse.class, TyphoidRats.class, WitnessProtection.class, InvisibleStalker.class})
class SeverTheBloodlineTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target creature")
    void exilesTargetCreature() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new SeverTheBloodline()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
    }

    @Test
    @DisplayName("Exiles all creatures with the same name across both players' battlefields")
    void exilesAllCreaturesWithSameName() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.addToBattlefield(player2, new TyphoidRats());
        harness.setHand(player1, List.of(new SeverTheBloodline()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castAndResolveSorcery(player1, 0, targetId);

        // All Walking Corpse should be exiled from both players' battlefields
        harness.assertNotOnBattlefield(player1, "Walking Corpse");
        harness.assertNotOnBattlefield(player2, "Walking Corpse");

        // Typhoid Rats should still be on the battlefield
        harness.assertOnBattlefield(player2, "Typhoid Rats");

        // All Walking Corpse should be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Walking Corpse"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Fizzles when target creature is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new SeverTheBloodline()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castSorcery(player1, 0, targetId);

        // Remove all creatures before resolution
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Flashback casts from graveyard and exiles the spell afterwards")
    void flashbackExilesSpellAfterResolving() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setGraveyard(player1, List.of(new SeverTheBloodline()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        UUID targetId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castAndResolveFlashback(player1, 0, targetId);

        // Target creature should be exiled
        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));

        // Flashback spell should be exiled, not in graveyard
        harness.assertNotInGraveyard(player1, "Sever the Bloodline");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sever the Bloodline"));
    }

    @Test
    @DisplayName("A lost target leaves another creature with the same name untouched")
    void lostTargetDoesNotExileNamesake() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new SeverTheBloodline()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, target));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(other);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Sever the Bloodline");
    }

    @Test
    @DisplayName("Flashback exiles the spell even when its target becomes illegal")
    void flashbackWithLostTargetExilesSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setGraveyard(player1, List.of(new SeverTheBloodline()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castFlashback(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, target));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(other);
        harness.assertNotInGraveyard(player1, "Sever the Bloodline");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sever the Bloodline"));
    }

    @Test
    @DisplayName("Name matching uses current names rather than printed names")
    void matchesCurrentNames() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent renamedOther = harness.addToBattlefieldAndReturn(player1, new TyphoidRats());
        Permanent unchanged = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new WitnessProtection(), new WitnessProtection()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, renamedOther.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new SeverTheBloodline()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(unchanged).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(renamedOther);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Typhoid Rats"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Walking Corpse")).hasSize(1);
    }

    @Test
    @DisplayName("Targeting a nameless face-down creature exiles only that creature")
    void faceDownTargetDoesNotMatchHiddenNames() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent otherFaceDown = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent faceUp = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        otherFaceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new SeverTheBloodline()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherFaceDown).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(faceUp);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Other creatures with the same name are exiled despite hexproof")
    void exilesUntargetedNamesakeWithHexproof() {
        harness.addToBattlefield(player1, new InvisibleStalker());
        harness.addToBattlefield(player2, new InvisibleStalker());
        harness.setHand(player1, List.of(new SeverTheBloodline()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Invisible Stalker"));

        harness.assertNotOnBattlefield(player1, "Invisible Stalker");
        harness.assertNotOnBattlefield(player2, "Invisible Stalker");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Invisible Stalker"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Invisible Stalker"));
    }
}
