package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvacynsCollarTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has static +1/+0 boost and vigilance for equipped creature")
    void hasStaticEffects() {
        AvacynsCollar card = new AvacynsCollar();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(StaticBoostEffect.class);
        StaticBoostEffect boost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).get(0);
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
        assertThat(boost.scope()).isEqualTo(GrantScope.EQUIPPED_CREATURE);

        assertThat(card.getEffects(EffectSlot.STATIC).get(1)).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect vigilance = (GrantKeywordEffect) card.getEffects(EffectSlot.STATIC).get(1);
        assertThat(vigilance.keywords()).containsExactly(Keyword.VIGILANCE);
        assertThat(vigilance.scope()).isEqualTo(GrantScope.EQUIPPED_CREATURE);
    }

    @Test
    @DisplayName("Has SubtypeConditionalEffect(HUMAN) death trigger wrapping CreateTokenEffect")
    void hasDeathTrigger() {
        AvacynsCollar card = new AvacynsCollar();

        assertThat(card.getEffects(EffectSlot.ON_EQUIPPED_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_EQUIPPED_CREATURE_DIES).getFirst())
                .isInstanceOf(SubtypeConditionalEffect.class);
        SubtypeConditionalEffect conditional = (SubtypeConditionalEffect)
                card.getEffects(EffectSlot.ON_EQUIPPED_CREATURE_DIES).getFirst();
        assertThat(conditional.subtype()).isEqualTo(CardSubtype.HUMAN);
        assertThat(conditional.wrapped()).isInstanceOf(CreateTokenEffect.class);
    }

    // ===== Static boost =====

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

    // ===== Death trigger — Human =====

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

    // ===== Death trigger — Non-Human =====

    @Test
    @DisplayName("Does NOT create token when equipped non-Human creature dies")
    void noTokenWhenNonHumanDies() {
        Permanent creature = addNonHumanCreature(player1);
        Permanent collar = addCollarReady(player1);
        collar.setAttachedTo(creature.getId());

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        killCreature(creature);

        // No spirit token — battlefield should only have the collar remaining
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spirit"));
    }

    // ===== Death trigger — not equipped =====

    @Test
    @DisplayName("No token when unequipped Human creature dies")
    void noTokenWhenUnequippedHumanDies() {
        Permanent creature = addHumanCreature(player1);
        addCollarReady(player1); // not attached

        killCreature(creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spirit"));
    }

    // ===== Equipment persists =====

    @Test
    @DisplayName("Equipment stays on battlefield after equipped creature dies")
    void equipmentPersistsAfterDeath() {
        Permanent creature = addHumanCreature(player1);
        Permanent collar = addCollarReady(player1);
        collar.setAttachedTo(creature.getId());

        killCreature(creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Avacyn's Collar"));
        assertThat(collar.getAttachedTo()).isNull();
    }

    // ===== Helpers =====

    private Permanent addCollarReady(Player player) {
        Permanent perm = new Permanent(new AvacynsCollar());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addHumanCreature(Player player) {
        Permanent perm = new Permanent(new EliteVanguard());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addNonHumanCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
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
