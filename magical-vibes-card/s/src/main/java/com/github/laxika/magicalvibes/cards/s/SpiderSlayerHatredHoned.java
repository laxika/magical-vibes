package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyDamagedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "175")
@CardRegistration(set = "OM1", collectorNumber = "168")
public class SpiderSlayerHatredHoned extends Card {

    public SpiderSlayerHatredHoned() {
        addTrigger(Triggers.creatureYouControlDealsDamageToCreature(),
                new DestroyDamagedCreatureEffect(new PermanentHasSubtypePredicate(CardSubtype.SPIDER)));

        addActivatedAbility(AbilityZones.GRAVEYARD, new ActivatedAbility(
                false,
                "{6}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenEffect(2, "Robot", 1, 1, null,
                                List.of(CardSubtype.ROBOT), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT), true)
                ),
                "{6}, Exile this card from your graveyard: Create two tapped 1/1 colorless Robot artifact creature tokens with flying."
        ));
    }
}
