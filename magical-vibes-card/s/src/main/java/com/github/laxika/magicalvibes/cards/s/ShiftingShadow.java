package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEC", collectorNumber = "109")
@CardRegistration(set = "C17", collectorNumber = "28")
public class ShiftingShadow extends Card {

    public ShiftingShadow() {
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        target(TargetFilters.creature())
                .addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.attachedCreature()))
                // Enchanted creature has "At the beginning of your upkeep, destroy this creature. Reveal cards from
                // the top of your library until you reveal a creature card. Put that card onto the battlefield and
                // attach Shifting Shadow to it, then put all other cards revealed this way on the bottom of your
                // library in a random order." "This creature" is the ability's source, Shifting Shadow the Aura
                // that granted it.
                .addStaticEffect(new GrantTriggeredAbilityEffect(new TriggeredAbility(
                        Triggers.upkeep(ActivePlayer.YOU),
                        SequenceEffect.of(
                                new DestroyTargetPermanentEffect(Subjects.source()),
                                new RevealCardsFromTopUntilEffect(creatureCard),
                                new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(creatureCard)),
                                new AttachSourceEquipmentToTargetCreatureEffect(Subjects.granter(),
                                        Subjects.returnedThisResolution()),
                                PutCardsIntoLibraryEffect.onBottomInRandomOrder(
                                        Subjects.restRevealedThisWay(creatureCard)))),
                        Subjects.attachedCreature()));
    }
}
