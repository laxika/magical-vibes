package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvacynsCollar.class, Deathmark.class, EliteVanguard.class, GrizzlyBears.class, Xenograft.class})
class AvacynsCollarTest extends BaseCardTest {


    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addHumanCreature(player1);
        Permanent collar = addCollarReady(player1);
        collar.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3); // EliteVanguard 2/1 -> 3/1
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equipped creature has vigilance")
    void equippedCreatureHasVigilance() {
        Permanent creature = addHumanCreature(player1);
        Permanent collar = addCollarReady(player1);
        collar.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creature does not get boost or vigilance")
    void unequippedCreatureNoBoost() {
        Permanent creature = addHumanCreature(player1);
        addCollarReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }


    @Test
    @DisplayName("Creates 1/1 white Spirit token with flying when equipped Human dies")
    void createsTokenWhenEquippedHumanDies() {
        Permanent creature = addHumanCreature(player1);
        Permanent collar = addCollarReady(player1);
        collar.setAttachedTo(creature.getId());

        killCreature(creature);

        // Spirit token should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spirit")
                        && p.getCard().getSubtypes().contains(CardSubtype.SPIRIT)
                        && p.getCard().getKeywords().contains(Keyword.FLYING)
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1);
    }


    @Test
    @DisplayName("Does NOT create token when equipped non-Human creature dies")
    void noTokenWhenNonHumanDies() {
        Permanent creature = addNonHumanCreature(player1);
        Permanent collar = addCollarReady(player1);
        collar.setAttachedTo(creature.getId());

        killCreature(creature);

        // No spirit token — battlefield should only have the collar remaining
        harness.assertNotOnBattlefield(player1, "Spirit");
    }


    @Test
    @DisplayName("No token when unequipped Human creature dies")
    void noTokenWhenUnequippedHumanDies() {
        Permanent creature = addHumanCreature(player1);
        addCollarReady(player1); // not attached

        killCreature(creature);

        harness.assertNotOnBattlefield(player1, "Spirit");
    }


    @Test
    @DisplayName("Equipment stays on battlefield after equipped creature dies")
    void equipmentPersistsAfterDeath() {
        Permanent creature = addHumanCreature(player1);
        Permanent collar = addCollarReady(player1);
        collar.setAttachedTo(creature.getId());

        killCreature(creature);

        harness.assertOnBattlefield(player1, "Avacyn's Collar");
        assertThat(collar.getAttachedTo()).isNull();
    }


    private Permanent addCollarReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AvacynsCollar());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addHumanCreature(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new EliteVanguard());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addNonHumanCreature(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    void equipAttachesAndMovesBonusesForTwoMana() {
        Permanent collar = addCollarReady(player1);
        Permanent human = addHumanCreature(player1);
        Permanent bear = addNonHumanCreature(player1);
        collar.setAttachedTo(human.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(collar.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, human, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void collarControllerReceivesTokenForOpponentsHuman() {
        Permanent human = addHumanCreature(player2);
        Permanent collar = addCollarReady(player1);
        collar.setAttachedTo(human.getId());

        killCreature(human);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().getColor())
                            .isEqualTo(CardColor.WHITE);
                    assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
                    assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
                });
        harness.assertNotOnBattlefield(player2, "Spirit");
    }

    @Test
    void creatureMadeHumanByXenograftCreatesTokenWhenItDies() {
        Permanent xenograft = harness.addToBattlefieldAndReturn(player1, new Xenograft());
        xenograft.setChosenSubtype(CardSubtype.HUMAN);
        Permanent bear = addNonHumanCreature(player1);
        Permanent collar = addCollarReady(player1);
        collar.setAttachedTo(bear.getId());
        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.HUMAN)).isTrue();

        killCreature(bear);

        harness.assertOnBattlefield(player1, "Spirit");
    }

    private void killCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorcery(player2, 0, creature.getId());
        harness.passBothPriorities(); // resolve Deathmark — creature dies, trigger goes on stack
        harness.passBothPriorities(); // resolve death trigger (if any)
    }
}
