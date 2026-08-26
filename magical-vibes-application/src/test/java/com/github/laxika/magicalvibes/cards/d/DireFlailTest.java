package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DireFlail.class, DireBlunderbuss.class, DarksteelRelic.class, GrizzlyBears.class})
class DireFlailTest extends BaseCardTest {

    @Test
    @DisplayName("Dire Flail boosts its equipped creature")
    void direFlailBoostsEquippedCreature() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent flail = addReady(player1, new DireFlail());
        flail.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Craft exiles another artifact and returns Dire Flail transformed")
    void craftReturnsTransformed() {
        Permanent flail = addReady(player1, new DireFlail());
        Permanent material = addReady(player1, new DarksteelRelic());
        addCraftMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(flail, material);
        assertThat(gd.findExiledCard(material.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof DireBlunderbuss);
    }

    @Test
    @DisplayName("Craft exiles an artifact card from the graveyard and returns transformed")
    void craftReturnsTransformedWithGraveyardArtifact() {
        Permanent flail = addReady(player1, new DireFlail());
        DarksteelRelic material = new DarksteelRelic();
        harness.setGraveyard(player1, List.of(material));
        addCraftMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(flail);
        assertThat(gd.findExiledCard(material.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof DireBlunderbuss);
    }

    @Test
    @DisplayName("Dire Blunderbuss may sacrifice another artifact and deal damage equal to the equipped creature's power")
    void backFaceAttackTriggerSacrificesArtifactAndDealsPowerDamage() {
        Permanent blunderbuss = addTransformedBlunderbuss();
        Permanent creature = addReady(player1, new GrizzlyBears());
        blunderbuss.setAttachedTo(creature.getId());
        Permanent material = addReady(player1, new DarksteelRelic());
        Permanent target = addReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice sacrificeChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(sacrificeChoice.validIds()).containsExactly(material.getId());
        harness.handlePermanentChosen(player1, material.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Darksteel Relic");
        assertThat(blunderbuss.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Declining Dire Blunderbuss's attack trigger does nothing")
    void decliningBackFaceAttackTriggerDoesNothing() {
        Permanent blunderbuss = addTransformedBlunderbuss();
        Permanent creature = addReady(player1, new GrizzlyBears());
        blunderbuss.setAttachedTo(creature.getId());
        Permanent material = addReady(player1, new DarksteelRelic());
        Permanent target = addReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(material);
    }

    private void addCraftMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent addTransformedBlunderbuss() {
        DireFlail front = new DireFlail();
        Permanent blunderbuss = new Permanent(front);
        blunderbuss.setCard(front.getBackFaceCard());
        blunderbuss.setTransformed(true);
        blunderbuss.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(blunderbuss);
        return blunderbuss;
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
