package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LibrarySelectionFollowUp;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SourcePermanentDealsPowerDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.UUID;

@CardRegistration(set = "M19", collectorNumber = "209")
public class ViviensInvocation extends Card {

    private static final LibrarySelectionFollowUp DAMAGE_FOLLOW_UP = new LibrarySelectionFollowUp() {
        @Override
        public CardEffect createEffect(List<UUID> selectedPermanentIds) {
            return new QueueReflexiveAbilityEffect(
                    new SourcePermanentDealsPowerDamageToTargetCreatureEffect(new PermanentAllOfPredicate(List.of(
                            new PermanentIsCreaturePredicate(),
                            new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())))),
                    selectedPermanentIds.getFirst());
        }

        @Override
        public String prompt() {
            return "Have the creature deal damage equal to its power to target creature an opponent controls?";
        }

        @Override
        public boolean optional() {
            return false;
        }
    };

    public ViviensInvocation() {
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.mayPutMatchingOntoBattlefieldRestOnBottomRandom(7,
                        new CardTypePredicate(CardType.CREATURE), DAMAGE_FOLLOW_UP));
    }
}
