package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

import java.util.List;

@CardRegistration(set = "NEC", collectorNumber = "7")
@CardRegistration(set = "NEC", collectorNumber = "41")
public class IronsoulEnforcer extends Card {

    public IronsoulEnforcer() {
        var thisCreatureOrCommander = new PermanentAnyOfPredicate(List.of(
                new PermanentIsSourceCardPredicate(),
                new PermanentIsCommanderPredicate()));
        var returnArtifact = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.ARTIFACT))
                .targetGraveyard(true)
                .build();

        // Whenever this creature or a commander you control attacks alone, return target artifact
        // card from your graveyard to the battlefield. The Enforcer itself is the ability's source,
        // so each Enforcer you control triggers separately when a commander attacks alone.
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                new AllOf(List.of(new AttacksAlone(), new HasAttacker(thisCreatureOrCommander))),
                returnArtifact));
    }
}
