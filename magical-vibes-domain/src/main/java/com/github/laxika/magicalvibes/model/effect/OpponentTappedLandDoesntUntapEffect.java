package com.github.laxika.magicalvibes.model.effect;

/**
 * Land-tap trigger-condition record: whenever an opponent taps a land for mana, that land doesn't untap during its
 * controller's next untap step. Used by Vorinclex, Voice of Hunger. The land-tap collector judges it by class (the
 * tapping player an opponent of the source's controller) and puts {@link #triggeredEffect()} on the stack with the
 * tapped land as the entry's triggering permanent.
 */
public record OpponentTappedLandDoesntUntapEffect() implements CardEffect {

    /** "That land doesn't untap during its controller's next untap step." */
    public CardEffect triggeredEffect() {
        return new SkipNextUntapEffect(TapUntapScope.TRIGGERING);
    }
}
