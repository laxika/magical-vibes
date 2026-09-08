package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "189")
public class GalazethPrismari extends Card {

    public GalazethPrismari() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofTreasureToken(1));

        ActivatedAbility artifactManaAbility = new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.INSTANT_SORCERY_ONLY)),
                "{T}: Add one mana of any color. Spend this mana only to cast an instant or sorcery spell."
        );
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                artifactManaAbility,
                GrantScope.OWN_PERMANENTS,
                new PermanentIsArtifactPredicate()
        ));
    }
}
