package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowHarrier;
import com.github.laxika.magicalvibes.cards.k.KithkinGreatheart;
import com.github.laxika.magicalvibes.cards.p.ProfaneCommand;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellstutterSprite.class, GoldmeadowHarrier.class, KithkinGreatheart.class,
        Tarfire.class, ProfaneCommand.class})
class SpellstutterSpriteTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell whose mana value is within the Faerie count (X=1 counts itself)")
    void countersSpellWithinFaerieCount() {
        GoldmeadowHarrier harrier = new GoldmeadowHarrier();
        SpellstutterSprite sprite = new SpellstutterSprite();
        harness.setHand(player1, List.of(harrier, sprite));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        // Cast Goldmeadow Harrier (mana value 1), then flash in Spellstutter Sprite in response.
        harness.castCreature(player1, 0);
        harness.castCreature(player1, 0);
        // Resolve the Sprite → it enters, and its ETB spell-target trigger offers the Harrier
        // (X = 1, the Sprite counts itself).
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(harrier.getId());

        harness.handlePermanentChosen(player1, harrier.getId());
        // Resolve the ETB trigger → counter Goldmeadow Harrier.
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(harrier.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(harrier.getId()));
    }

    @Test
    @DisplayName("Cannot target a spell whose mana value exceeds the Faerie count")
    void cannotTargetSpellAboveFaerieCount() {
        KithkinGreatheart greatheart = new KithkinGreatheart();
        SpellstutterSprite sprite = new SpellstutterSprite();
        harness.setHand(player1, List.of(greatheart, sprite));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        // Kithkin Greatheart has mana value 2; with only the Sprite as a Faerie (X = 1) it is not a legal target.
        harness.castCreature(player1, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // No valid target → the ETB is skipped, nothing to choose.
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        // Kithkin Greatheart was not countered — it never reached a graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(greatheart.getId()));
    }

    @Test
    @DisplayName("A larger spell becomes counterable when more Faeries are controlled")
    void countersLargerSpellWithMoreFaeries() {
        // A second Faerie already on the battlefield raises X to 2.
        harness.addToBattlefield(player1, new SpellstutterSprite());

        KithkinGreatheart greatheart = new KithkinGreatheart();
        SpellstutterSprite sprite = new SpellstutterSprite();
        harness.setHand(player1, List.of(greatheart, sprite));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castCreature(player1, 0);
        // Sprite enters → X = 2 (itself + the pre-placed Faerie), so Kithkin Greatheart (MV 2) is legal.
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(greatheart.getId());

        harness.handlePermanentChosen(player1, greatheart.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(greatheart.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(greatheart.getId()));
    }

    @Test
    @DisplayName("Counters a noncreature spell within the Faerie count")
    void countersNoncreatureSpellWithinFaerieCount() {
        Tarfire tarfire = new Tarfire();
        SpellstutterSprite sprite = new SpellstutterSprite();
        harness.setHand(player1, List.of(tarfire, sprite));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, player1.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(tarfire.getId());

        harness.handlePermanentChosen(player1, tarfire.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(tarfire.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not target an X spell whose chosen mana value exceeds the Faerie count")
    void doesNotTargetXSpellAboveFaerieCount() {
        harness.addToBattlefield(player1, new SpellstutterSprite());
        Permanent harrier = harness.addToBattlefieldAndReturn(player1, new GoldmeadowHarrier());
        ProfaneCommand command = new ProfaneCommand();
        SpellstutterSprite sprite = new SpellstutterSprite();
        harness.setHand(player1, List.of(command, sprite));
        harness.addMana(player1, ManaColor.BLACK, 3); // X=1 + {B}{B}
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLife(player2, 20);

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{0, 2}, 1,
                List.of(player2.getId(), harrier.getId()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Profane Command's mana value on the stack is 3 (X=1 plus {B}{B}), while Sprite counts 2 Faeries.
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(command.getId()));
    }
}
